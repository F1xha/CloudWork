param([Parameter(Mandatory=$true)][string]$ApiBaseUrl)
$ErrorActionPreference = 'Stop'
$ApiBaseUrl = $ApiBaseUrl.TrimEnd('/')
$uri = [Uri]$ApiBaseUrl
if ($uri.Scheme -ne 'https' -and $uri.Host -ne 'localhost') { throw 'Usa HTTPS fuera de localhost.' }
$report = New-Object System.Collections.Generic.List[string]
$report.Add('# Evidencia de ejecución API')
$report.Add('Fecha UTC: ' + [DateTime]::UtcNow.ToString('o'))
$report.Add('API: ' + $ApiBaseUrl)
$report.Add('')
$report.Add('| Caso | Esperado | Observado | Resultado |')
$report.Add('|---|---|---|---|')
$script:fallos = 0
function Test-Api([string]$Caso,[string]$Path,[int]$Esperado,[string]$Token='') {
 $headers = @{}
 if ($Token) { $headers.Authorization = 'Bearer ' + $Token }
 try { $response=Invoke-WebRequest -Uri ($ApiBaseUrl+$Path) -Headers $headers -UseBasicParsing; $status=[int]$response.StatusCode }
 catch { if ($_.Exception.Response) { $status=[int]$_.Exception.Response.StatusCode } else { $status=0 } }
 $resultado=if($status -eq $Esperado){'OK'}else{'FALLO'}
 if($resultado -eq 'FALLO') { $script:fallos++ }
 $line='| '+$Caso+' | '+$Esperado+' | '+$status+' | '+$resultado+' |'
 $report.Add($line)
 Write-Host ($Caso+': '+$status+' ('+$resultado+')')
}
function Read-Token([string]$Prompt) {
 $secure=Read-Host $Prompt -AsSecureString
 $ptr=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
 try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr).Trim() }
 finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}
Test-Api 'Publico' '/public/catalogo' 200
Test-Api 'Sin token' '/incidencias' 401
Test-Api 'Token manipulado' '/incidencias' 401 'invalido'
$user=Read-Token 'Pega SOLO el access token del usuario User (entrada oculta)'
Test-Api 'Lectura User' '/incidencias' 200 $user
Test-Api 'User en Admin' '/admin/reporte' 403 $user
$user=$null
$admin=Read-Token 'Pega SOLO el access token del usuario Admin (entrada oculta)'
Test-Api 'Admin en Admin' '/admin/reporte' 200 $admin
$admin=$null
$report.Add('')
$report.Add('Logout, creacion, edicion, persistencia RDS y renovacion MSAL: completar manualmente en VALIDACION-E2E.md.')
$folder=Join-Path $PSScriptRoot '../evidencias-privadas'
New-Item -ItemType Directory -Force $folder | Out-Null
$file=Join-Path $folder ('api-'+[DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss')+'.md')
$report | Set-Content -Encoding UTF8 $file
Write-Host ('Evidencia sin tokens: '+$file)
if($script:fallos -gt 0){exit 1}
