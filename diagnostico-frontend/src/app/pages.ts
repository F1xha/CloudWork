import { Component,inject,OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService,Categoria,Incidencia,Claims,mensajeError } from './api.service';
import { MsalService } from '@azure/msal-angular';
@Component({standalone:true,imports:[CommonModule],template:`
<p class="eyebrow">MESA DE AYUDA</p><h1>Resuelve, registra y da seguimiento.</h1>
<p>Consulta las categorías de atención. Inicia sesión para gestionar las incidencias de tu equipo.</p>
<p role="status" class="status">{{mensaje}}</p>
<div class="grid"><article *ngFor="let c of categorias"><span>CATEGORÍA {{c.id}}</span><h2>{{c.nombre}}</h2></article></div>
<p *ngIf="!categorias.length">Aún no hay categorías. El administrador puede crearlas desde su panel.</p>
<h2>Comprobar el acceso</h2><p>Estas comprobaciones envían solicitudes sin sesión o con un token inválido, incluso si estás conectado.</p>
<div class="actions"><button (click)="sinToken()">Probar sin token (401)</button><button (click)="invalido()">Probar token inválido (401)</button></div>`})
export class CatalogoComponent implements OnInit {
 api=inject(ApiService);categorias:Categoria[]=[];mensaje='';
 ngOnInit(){this.api.catalogo().subscribe({next:c=>{this.categorias=c;this.mensaje='200 — Catálogo público disponible.';},error:e=>this.mensaje=mensajeError(e)});}
 sinToken(){this.api.sinToken().subscribe({next:()=>this.mensaje='Respuesta inesperada: la API permitió una consulta sin token.',error:e=>this.mensaje=mensajeError(e)});}
 invalido(){this.api.invalido().subscribe({next:()=>this.mensaje='Respuesta inesperada: la API aceptó un token inválido.',error:e=>this.mensaje=mensajeError(e)});}
}
@Component({standalone:true,imports:[CommonModule,FormsModule],template:`
<p class="eyebrow">GESTIÓN</p><h1>Incidencias</h1><p role="status" class="status">{{mensaje}}</p>
<form #form="ngForm" (ngSubmit)="guardar()">
<h2>{{editId ? 'Editar incidencia' : 'Registrar incidencia'}}</h2>
<label>Título<input name="titulo" [(ngModel)]="datos.titulo" required maxlength="150"></label>
<label>Descripción<textarea name="descripcion" [(ngModel)]="datos.descripcion" required maxlength="2000"></textarea></label>
<div class="grid"><label>Categoría<select name="categoriaId" [(ngModel)]="datos.categoriaId" required><option [ngValue]="null">Seleccionar</option><option *ngFor="let c of categorias" [ngValue]="c.id">{{c.nombre}}</option></select></label>
<label>Prioridad<select name="prioridad" [(ngModel)]="datos.prioridad"><option>BAJA</option><option>MEDIA</option><option>ALTA</option></select></label>
<label *ngIf="editId">Estado<select name="estado" [(ngModel)]="datos.estado"><option>ABIERTA</option><option>EN_PROGRESO</option><option>RESUELTA</option></select></label></div>
<div class="actions"><button [disabled]="form.invalid || busy" type="submit">{{busy ? 'Guardando…' : 'Guardar'}}</button><button type="button" (click)="limpiar()">Limpiar</button></div></form>
<div class="section-heading"><h2>Seguimiento</h2><button (click)="cargar()">Actualizar</button></div>
<div class="table-wrap"><table><thead><tr><th>ID</th><th>Incidencia</th><th>Categoría</th><th>Prioridad</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>
<tr *ngFor="let i of incidencias"><td>{{i.id}}</td><td><strong>{{i.titulo}}</strong><p>{{i.descripcion}}</p></td><td>{{i.categoria}}</td><td>{{i.prioridad}}</td><td>{{i.estado}}</td><td><button (click)="editar(i)">Editar</button><button *ngIf="admin" (click)="eliminar(i)">Eliminar</button></td></tr></tbody></table></div><p *ngIf="!incidencias.length">No hay incidencias registradas.</p>`})
export class IncidenciasComponent implements OnInit {
 api=inject(ApiService);categorias:Categoria[]=[];incidencias:Incidencia[]=[];mensaje='';busy=false;admin=false;editId:number|null=null;
 datos={titulo:'',descripcion:'',categoriaId:null as number|null,prioridad:'MEDIA',estado:'ABIERTA'};
 ngOnInit(){this.cargar();this.api.catalogo().subscribe({next:c=>this.categorias=c,error:e=>this.mensaje=mensajeError(e)});this.api.me().subscribe({next:c=>this.admin=!!c.roles?.includes('Admin'),error:e=>this.mensaje=mensajeError(e)});}
 cargar(show=true){this.api.listar().subscribe({next:d=>{this.incidencias=d;if(show)this.mensaje='200 — Incidencias consultadas.';},error:e=>this.mensaje=mensajeError(e)});}
 guardar(){if(this.busy)return;this.busy=true;const edit=!!this.editId;
  const request=this.editId?this.api.editar(this.editId,this.datos):this.api.crear(this.datos);
  request.subscribe({next:()=>{this.busy=false;this.limpiar();this.mensaje=edit?'200 — Cambios guardados.':'201 — Incidencia creada y persistida.';this.cargar(false);},error:e=>{this.busy=false;this.mensaje=mensajeError(e);}});
 }
 editar(i:Incidencia){this.editId=i.id;this.datos={titulo:i.titulo,descripcion:i.descripcion,categoriaId:i.categoriaId,prioridad:i.prioridad,estado:i.estado};}
 limpiar(){this.editId=null;this.datos={titulo:'',descripcion:'',categoriaId:null,prioridad:'MEDIA',estado:'ABIERTA'};}
 eliminar(i:Incidencia){if(!confirm(`¿Eliminar la incidencia ${i.id}?`))return;this.api.eliminar(i.id).subscribe({next:()=>{this.mensaje='204 — Incidencia eliminada.';this.cargar(false);},error:e=>this.mensaje=mensajeError(e)});}
}
@Component({standalone:true,imports:[CommonModule,FormsModule],template:`
<p class="eyebrow">ADMINISTRACIÓN</p><h1>Reporte del equipo</h1><p class="status" role="status">{{mensaje}}</p>
<div class="grid"><article *ngFor="let r of reporte | keyvalue"><span>{{r.key}}</span><h2>{{r.value}}</h2></article></div>
<form #form="ngForm" (ngSubmit)="crear()"><h2>Nueva categoría</h2><label>Nombre<input name="nombre" [(ngModel)]="nombre" required maxlength="100"></label><button [disabled]="form.invalid || busy">Crear categoría</button></form>`})
export class AdminComponent implements OnInit {
 api=inject(ApiService);reporte:Record<string,number>={};mensaje='';nombre='';busy=false;
 ngOnInit(){this.api.reporte().subscribe({next:r=>{this.reporte=r;this.mensaje='200 — Acceso de administrador confirmado.';},error:e=>this.mensaje=mensajeError(e)});}
 crear(){if(this.busy)return;this.busy=true;this.api.categoria(this.nombre).subscribe({next:()=>{this.busy=false;this.nombre='';this.mensaje='201 — Categoría creada.';},error:e=>{this.busy=false;this.mensaje=mensajeError(e);}});}
}
@Component({standalone:true,imports:[CommonModule],template:`
<h1>Mi cuenta</h1><p>Claims del access token verificados por el backend. No se muestra el token completo.</p>
<p role="status" class="status">{{mensaje}}</p><pre *ngIf="claims">{{claims | json}}</pre>
<h2>Comprobar permisos</h2><p>Esta solicitud llega al backend para demostrar el 403 real con User y el 200 con Admin.</p><button (click)="probarAdmin()">Consultar reporte de administrador</button>`})
export class CuentaComponent implements OnInit {
 api=inject(ApiService);claims:Claims|null=null;mensaje='';
 ngOnInit(){this.api.me().subscribe({next:c=>{this.claims=c;this.mensaje='200 — Identidad y permisos verificados.';},error:e=>this.mensaje=mensajeError(e)});}
 probarAdmin(){this.api.reporte().subscribe({next:()=>this.mensaje='200 — El backend permitió el acceso de administrador.',error:e=>this.mensaje=mensajeError(e)});}
}
@Component({standalone:true,template:'<h1>Acceso denegado</h1><p>No posees el rol de Administrador o el permiso de lectura requerido. Puedes continuar trabajando en Incidencias.</p>'})
export class ForbiddenComponent {}
