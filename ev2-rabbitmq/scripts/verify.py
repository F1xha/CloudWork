"""Integration test against the actual Docker Compose stack; Python standard library."""
import json, time, urllib.request, urllib.error, base64, subprocess
from pathlib import Path
OUT=Path("evidence"); OUT.mkdir(exist_ok=True)
AUTH=base64.b64encode(b"ev2:ev2-local-demo").decode()
summary=[]
# Avoid claiming PASS from a previous run if the current run fails.
for old in OUT.glob("*.json"): old.unlink()
for name in ["colas-pendientes.png", "exchange-bindings.png", "colas-consumidas.png"]:
    (OUT/name).unlink(missing_ok=True)
def request(port,path,body=None,management=False):
    data=None if body is None else json.dumps(body).encode()
    headers={"Content-Type":"application/json"}
    if management: headers["Authorization"]="Basic "+AUTH
    req=urllib.request.Request(f"http://localhost:{port}{path}",data=data,headers=headers)
    try:
        with urllib.request.urlopen(req,timeout=8) as r: return r.status,json.load(r)
    except urllib.error.HTTPError as e: return e.code,json.load(e)
def wait(check, seconds=120):
    deadline=time.time()+seconds
    while time.time()<deadline:
        try:
            result=check()
            if result: return result
        except (OSError,ValueError): pass
        time.sleep(1)
    raise AssertionError("Timeout esperando condición")
def save(name,value): (OUT/(name+".json")).write_text(json.dumps(value,indent=2,ensure_ascii=False))
def ok(name): summary.append(name); print("PASS "+name,flush=True)
def compose(*args): subprocess.run(["docker","compose",*args],check=True)
def screenshot(name,route):
    from playwright.sync_api import sync_playwright
    with sync_playwright() as p:
        browser=p.chromium.launch(headless=True,args=["--no-sandbox"])
        page=browser.new_page(viewport={"width":1440,"height":1000})
        page.goto("http://localhost:15672")
        page.locator('input[name="username"]').fill("ev2")
        page.locator('input[name="password"]').fill("ev2-local-demo")
        page.locator('input[type="submit"]').click()
        page.wait_for_selector('a[href="#/queues"]')
        page.goto("http://localhost:15672/"+route)
        page.wait_for_timeout(2500)
        page.screenshot(path=str(OUT/(name+".png")),full_page=True)
        browser.close()
for port in [8081,8082,8083,8084]: wait(lambda p=port: request(p,"/health")[0]==200)
wait(lambda: all(q.get("consumers",0)==1 for q in request(15672,"/api/queues",management=True)[1]) and len(request(15672,"/api/queues",management=True)[1])==2)
body={"titulo":"Impresora no imprime","descripcion":"Caso EV2 verificable","categoriaId":1,"prioridad":"ALTA"}
code,event=request(8081,"/api/incidencias",body)
assert code==201 and event["publicacion"]=="CONFIRMADA",(code,event)
save("01-solicitud-y-respuesta",{"request":body,"http":code,"response":event})
for port in [8083,8084]: wait(lambda p=port:any(x["EVENTO_ID"]==event["eventoId"] for x in request(p,"/procesados")[1]))
save("02-auditoria",request(8083,"/procesados")[1]); save("03-notificaciones",request(8084,"/procesados")[1]); ok("201 y mismo evento procesado por ambos consumers")
before=len(request(8081,"/api/incidencias")[1]); code,error=request(8081,"/api/incidencias",dict(body,categoriaId=999))
assert code==400 and len(request(8081,"/api/incidencias")[1])==before
save("04-categoria-invalida",{"http":code,"response":error});ok("Categoría inválida rechazada sin guardar")
code,error=request(8081,"/api/incidencias",dict(body,titulo="")); assert code==400;ok("Payload inválido rechazado")
compose("stop","auditoria","notificaciones")
code,delayed=request(8081,"/api/incidencias",dict(body,titulo="Consumers detenidos"))
assert code==201 and delayed["publicacion"]=="CONFIRMADA"
queues=wait(lambda:(lambda qs:qs if len(qs)==2 and all(q["messages_ready"]>=1 and q["consumers"]==0 for q in qs) else None)(request(15672,"/api/queues",management=True)[1]))
save("05-colas-con-mensajes",queues); save("06-respuesta-sin-consumers",delayed)
screenshot("colas-pendientes","#/queues")
screenshot("exchange-bindings","#/exchanges/%2F/incidencias.exchange")
ok("Respuesta inmediata con consumers detenidos y mensaje en ambas colas")
compose("start","auditoria","notificaciones")
for port in [8083,8084]: wait(lambda p=port:any(x["EVENTO_ID"]==delayed["eventoId"] for x in request(p,"/procesados")[1]))
wait(lambda:all(q["messages"]==0 for q in request(15672,"/api/queues",management=True)[1]))
save("07-colas-consumidas",request(15672,"/api/queues",management=True)[1]); screenshot("colas-consumidas","#/queues");ok("Mensajes acumulados procesados al reiniciar consumers")
compose("stop","categorias")
before=len(request(8081,"/api/incidencias")[1]); code,error=request(8081,"/api/incidencias",body)
assert code==503 and len(request(8081,"/api/incidencias")[1])==before
save("08-categorias-caido",{"http":code,"response":error});ok("Dependencia síncrona caída impide guardar y devuelve 503")
compose("start","categorias")
save("09-exchange",request(15672,"/api/exchanges/%2F/incidencias.exchange",management=True)[1])
save("10-bindings",request(15672,"/api/exchanges/%2F/incidencias.exchange/bindings/source",management=True)[1])
save("11-auditoria-final",request(8083,"/procesados")[1]);save("12-notificaciones-final",request(8084,"/procesados")[1])
save("resultado",{"status":"PASS","tests":summary,"eventoInicial":event["eventoId"],"eventoDiferido":delayed["eventoId"]})
