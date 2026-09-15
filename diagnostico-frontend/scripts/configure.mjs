import { createInterface } from 'node:readline/promises';
import { stdin, stdout } from 'node:process';
import { writeFile } from 'node:fs/promises';
const r = createInterface({input:stdin,output:stdout});
try {
 const tenantId=(await r.question('Directory (tenant) ID [9cf112db-fbb5-4a2c-9f50-b6b028a24f5c]: ')).trim() || '9cf112db-fbb5-4a2c-9f50-b6b028a24f5c';
 const clientId=(await r.question('Application (client) ID de SPA [df6c2446-af20-4edd-bd5d-d3e72ede9bbc]: ')).trim() || 'df6c2446-af20-4edd-bd5d-d3e72ede9bbc';
 const backend=(await r.question('Application (client) ID de API [29fad832-f772-4917-ac2a-c98db303d04c]: ')).trim() || '29fad832-f772-4917-ac2a-c98db303d04c';
 const guid=/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
 if(![tenantId,clientId,backend].every(x=>guid.test(x))) throw new Error('Los tres ID deben ser GUID válidos.');
 const apiBaseUrl=(await r.question('URL de API terminada en /api [http://localhost:8080/api]: ')).trim().replace(/\/$/,'') || 'http://localhost:8080/api';
 const redirectUri=(await r.question('Redirect URI [http://localhost:4200/]: ')).trim() || 'http://localhost:4200/';
 for(const value of [apiBaseUrl,redirectUri]) { const u=new URL(value); if(u.protocol!=='https:' && !(u.protocol==='http:' && u.hostname==='localhost')) throw new Error('Usa HTTPS fuera de localhost.'); }
 const config={apiBaseUrl,azure:{tenantId,clientId,redirectUri,apiScopeRead:`api://${backend}/recurso.read`,apiScopeWrite:`api://${backend}/recurso.write`}};
 await writeFile(new URL('../src/environments/environment.ts',import.meta.url),'export const environment = '+JSON.stringify(config,null,2)+';\n');
 console.log('Configuración guardada. Reinicia npm start o vuelve a compilar.');
} finally {r.close();}
