import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';
import { msalInstance } from './app/auth-config';
async function main(){
 await msalInstance.initialize();
 const response=await msalInstance.handleRedirectPromise();
 const account=response?.account ?? msalInstance.getActiveAccount() ?? msalInstance.getAllAccounts()[0];
 if(account) msalInstance.setActiveAccount(account);
 await bootstrapApplication(AppComponent,appConfig);
}
main().catch(()=>{document.body.textContent='No se pudo iniciar la aplicación. Verifica los ID y el redirect URI ejecutando npm run configure.';});
