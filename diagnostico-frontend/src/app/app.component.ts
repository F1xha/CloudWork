import { Component,inject,OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router,RouterLink,RouterLinkActive,RouterOutlet } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { scopes } from './auth-config';
@Component({selector:'app-root',standalone:true,imports:[CommonModule,RouterLink,RouterLinkActive,RouterOutlet],template:`
<header><a class="brand" routerLink="/">Diagnóstico<span>Mesa de ayuda</span></a><nav aria-label="Principal"><a routerLink="/">Catálogo</a><a routerLink="/incidencias" routerLinkActive="active">Incidencias</a><a routerLink="/cuenta" routerLinkActive="active">Mi cuenta</a><a routerLink="/admin" routerLinkActive="active">Administración</a></nav>
<div><small *ngIf="cuenta">{{cuenta}}</small><button *ngIf="!cuenta" (click)="login()">Iniciar sesión</button><button *ngIf="cuenta" (click)="logout()">Cerrar sesión</button></div></header>
<main><p *ngIf="error" role="alert">{{error}}</p><router-outlet /></main><footer>Diagnóstico · Desarrollo Cloud Native I · EV1</footer>`})
export class AppComponent implements OnInit {
 msal=inject(MsalService);router=inject(Router);error='';
 get cuenta(){return this.msal.instance.getActiveAccount()?.username;}
 ngOnInit(){this.msal.handleRedirectObservable().subscribe({next:r=>{if(r?.account)this.msal.instance.setActiveAccount(r.account);},error:()=>this.error='No se pudo completar el inicio de sesión. Revisa la configuración de Entra ID.'});}
 login(){this.msal.loginRedirect({scopes,prompt:'select_account'}).subscribe({error:()=>this.error='No se pudo iniciar sesión.'});}
 async logout(){
  const account=this.msal.instance.getActiveAccount();
  // Desmontar la vista protegida antes de salir del navegador hacia Entra ID.
  await this.router.navigateByUrl('/');
  this.msal.logoutRedirect({account:account??undefined}).subscribe({error:()=>this.error='No se pudo completar el cierre de sesión. Inténtalo nuevamente.'});
 }
}
