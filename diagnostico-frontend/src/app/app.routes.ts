import { inject } from '@angular/core';
import { CanActivateFn,Router,Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { map,catchError,of } from 'rxjs';
import { ApiService } from './api.service';
import {CatalogoComponent,IncidenciasComponent,AdminComponent,ForbiddenComponent,CuentaComponent} from './pages';
export const adminGuard:CanActivateFn=()=>{
 const router=inject(Router);
 // /me obtiene claims del ACCESS TOKEN ya validados por el Resource Server.
 return inject(ApiService).me().pipe(map(c=>c.roles?.includes('Admin')?true:router.createUrlTree(['/forbidden'])),catchError(()=>of(router.createUrlTree(['/forbidden']))));
};
export const routes:Routes=[
 {path:'',component:CatalogoComponent},
 {path:'incidencias',component:IncidenciasComponent,canActivate:[MsalGuard]},
 {path:'cuenta',component:CuentaComponent,canActivate:[MsalGuard]},
 {path:'admin',component:AdminComponent,canActivate:[MsalGuard,adminGuard]},
 {path:'forbidden',component:ForbiddenComponent},
 {path:'**',redirectTo:''}
];
