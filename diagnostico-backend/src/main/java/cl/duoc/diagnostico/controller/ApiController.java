package cl.duoc.diagnostico.controller;
import cl.duoc.diagnostico.service.DiagnosticoService;
import cl.duoc.diagnostico.dto.Contratos.*;
import cl.duoc.diagnostico.model.Categoria;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.net.URI;
import java.util.*;
@RestController @RequestMapping("/api")
public class ApiController {
 private final DiagnosticoService service;
 public ApiController(DiagnosticoService service){this.service=service;}
 @GetMapping("/public/health") public Map<String,String> salud(){return Map.of("estado","UP");}
 @GetMapping("/public/catalogo") public List<Categoria> catalogo(){return service.catalogo();}
 @GetMapping("/incidencias") public List<Vista> listar(){return service.listar();}
 @GetMapping("/incidencias/{id}") public Vista obtener(@PathVariable Long id){return service.obtener(id);}
 @PostMapping("/incidencias") public ResponseEntity<Vista> crear(@Valid @RequestBody Crear d){var v=service.crear(d);return ResponseEntity.created(URI.create("/api/incidencias/"+v.id())).body(v);}
 @PutMapping("/incidencias/{id}") public Vista editar(@PathVariable Long id,@Valid @RequestBody Editar d){return service.editar(id,d);}
 @DeleteMapping("/incidencias/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id){service.eliminar(id);return ResponseEntity.noContent().build();}
 @GetMapping("/admin/reporte") public Map<String,Long> reporte(){return service.reporte();}
 @PostMapping("/admin/categorias") public ResponseEntity<Categoria> categoria(@Valid @RequestBody CategoriaNueva d){var c=service.crearCategoria(d);return ResponseEntity.status(201).body(c);}
 @GetMapping("/me") public Map<String,Object> cuenta(@AuthenticationPrincipal Jwt jwt){
  Map<String,Object> result=new LinkedHashMap<>();
  for(String key:List.of("iss","aud","exp","scp","roles","preferred_username"))
   if(jwt.hasClaim(key)) result.put(key,jwt.getClaim(key));
  return result;
 }
}
