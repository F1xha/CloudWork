import { PublicClientApplication, InteractionType, BrowserCacheLocation } from '@azure/msal-browser';
import { MsalGuardConfiguration, MsalInterceptorConfiguration } from '@azure/msal-angular';
import { environment as e } from '../environments/environment';
export const scopes=[e.azure.apiScopeRead,e.azure.apiScopeWrite];
export const msalInstance=new PublicClientApplication({auth:{clientId:e.azure.clientId,authority:`https://login.microsoftonline.com/${e.azure.tenantId}`,redirectUri:e.azure.redirectUri,postLogoutRedirectUri:e.azure.redirectUri},cache:{cacheLocation:BrowserCacheLocation.SessionStorage}});
export function guardConfig():MsalGuardConfiguration{return {interactionType:InteractionType.Redirect,authRequest:{scopes}};}
export function interceptorConfig():MsalInterceptorConfiguration{
 const map=new Map<string,Array<string>|null>();
 map.set(`${e.apiBaseUrl}/public/*`,null);
 map.set(`${e.apiBaseUrl}/incidencias*`,scopes);
 map.set(`${e.apiBaseUrl}/admin/*`,scopes);
 map.set(`${e.apiBaseUrl}/me`,scopes);
 return {interactionType:InteractionType.Redirect,protectedResourceMap:map};
}
