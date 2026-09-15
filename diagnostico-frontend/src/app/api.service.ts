import { Injectable,inject } from '@angular/core';
import { HttpClient, HttpBackend, HttpErrorResponse } from '@angular/common/http';
import { environment as e } from '../environments/environment';
export interface Categoria {id:number;nombre:string;}
export interface Incidencia {id:number;titulo:string;descripcion:string;categoriaId:number;categoria:string;prioridad:string;estado:string;fechaCreacion:string;}
export interface Claims {iss:string;aud:string[];exp:string;scp:string;roles?:string[];preferred_username?:string;}
@Injectable({providedIn:'root'})
export class ApiService {
 private http=inject(HttpClient);
 private raw=new HttpClient(inject(HttpBackend));
 catalogo(){return this.http.get<Categoria[]>(`${e.apiBaseUrl}/public/catalogo`);}
 listar(){return this.http.get<Incidencia[]>(`${e.apiBaseUrl}/incidencias`);}
 crear(datos:unknown){return this.http.post<Incidencia>(`${e.apiBaseUrl}/incidencias`,datos);}
 editar(id:number,datos:unknown){return this.http.put<Incidencia>(`${e.apiBaseUrl}/incidencias/${id}`,datos);}
 eliminar(id:number){return this.http.delete(`${e.apiBaseUrl}/incidencias/${id}`);}
 reporte(){return this.http.get<Record<string,number>>(`${e.apiBaseUrl}/admin/reporte`);}
 categoria(nombre:string){return this.http.post<Categoria>(`${e.apiBaseUrl}/admin/categorias`,{nombre});}
 me(){return this.http.get<Claims>(`${e.apiBaseUrl}/me`);}
 sinToken(){return this.raw.get(`${e.apiBaseUrl}/incidencias`);}
 invalido(){return this.raw.get(`${e.apiBaseUrl}/incidencias`,{headers:{Authorization:'Bearer token-invalido'}});}
}
export function mensajeError(e:unknown):string {
 if(!(e instanceof HttpErrorResponse)) return 'No se pudo completar la autenticación. Intenta iniciar sesión nuevamente.';
 if(e.status===401) return '401 — Sesión ausente, token inválido o caducado. Inicia sesión nuevamente.';
 if(e.status===403) return '403 — Acceso denegado: no posees el rol de Administrador o el permiso requerido.';
 if(e.status===0) return 'Sin conexión con la API. Revisa el servidor, la URL y CORS.';
 return `${e.status} — ${e.error?.mensaje || 'No se pudo completar la solicitud. Revisa los datos.'}`;
}
