import { ApplicationConfig,importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient,withInterceptorsFromDi,HTTP_INTERCEPTORS } from '@angular/common/http';
import { MsalModule,MsalService,MsalGuard,MsalBroadcastService,MsalInterceptor,MSAL_INSTANCE,MSAL_GUARD_CONFIG,MSAL_INTERCEPTOR_CONFIG } from '@azure/msal-angular';
import { routes } from './app.routes';
import { msalInstance,guardConfig,interceptorConfig } from './auth-config';
export const appConfig:ApplicationConfig={providers:[provideRouter(routes),provideHttpClient(withInterceptorsFromDi()),importProvidersFrom(MsalModule),
 {provide:HTTP_INTERCEPTORS,useClass:MsalInterceptor,multi:true},
 {provide:MSAL_INSTANCE,useValue:msalInstance},{provide:MSAL_GUARD_CONFIG,useFactory:guardConfig},{provide:MSAL_INTERCEPTOR_CONFIG,useFactory:interceptorConfig},MsalService,MsalGuard,MsalBroadcastService]};
