$ErrorActionPreference = "Stop"
$body = @{ titulo="Impresora no imprime"; descripcion="Prueba de RabbitMQ EV2"; categoriaId=1; prioridad="ALTA" } | ConvertTo-Json
$resultado = Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/incidencias -ContentType "application/json" -Body $body
$resultado | Format-List
Start-Sleep -Seconds 2
Write-Host "AUDITORIA"
Invoke-RestMethod http://localhost:8083/procesados | ConvertTo-Json -Depth 5
Write-Host "NOTIFICACIONES"
Invoke-RestMethod http://localhost:8084/procesados | ConvertTo-Json -Depth 5
