# Requiere la API levantada: mvn spring-boot:run (puerto 8080)
# Uso:  powershell -File scripts/curl-api.ps1
# En PowerShell hay que usar curl.exe (curl a secas es otro comando).

$ErrorActionPreference = "Stop"
$base = "http://localhost:8080/api/goals"
$viajeId = "c0a80100-0000-4000-8000-000000000001"
$tmp = Join-Path $env:TEMP "bolsillo-curl"
New-Item -ItemType Directory -Force -Path $tmp | Out-Null

function Invoke-JsonPost([string]$url, [string]$json) {
    $file = Join-Path $tmp "body.json"
    [System.IO.File]::WriteAllText($file, $json)
    curl.exe -s -w "`nHTTP %{http_code}`n" -X POST $url -H "Content-Type: application/json" --data-binary "@$file"
    Write-Host ""
}

Write-Host "=== 1) GET lista  →  200 ==="
curl.exe -s -w "`nHTTP %{http_code}`n" $base
Write-Host ""

Write-Host "=== 2) POST crear meta  →  201 ==="
Invoke-JsonPost $base '{"name":"Meta curl demo","targetAmount":500000}'

Write-Host "=== 3) GET una meta (seed Viaje a Cartagena)  →  200 ==="
curl.exe -s -w "`nHTTP %{http_code}`n" "$base/$viajeId"
Write-Host ""

Write-Host "=== 4) POST abono valido  →  200 ==="
Invoke-JsonPost "$base/$viajeId/contributions" '{"amount":10000}'

Write-Host "=== 5) GET meta inexistente  →  404 GOAL_NOT_FOUND ==="
curl.exe -s -w "`nHTTP %{http_code}`n" "$base/c0a80100-0000-4000-8000-000000000099"
Write-Host ""

Write-Host "=== 6) POST abono 0  →  400 VALIDATION_ERROR ==="
Invoke-JsonPost "$base/$viajeId/contributions" '{"amount":0}'

Write-Host "=== 7) POST abono que se pasa del objetivo (Fondo emergencia)  →  422 ==="
$fondoId = "c0a80100-0000-4000-8000-000000000002"
Invoke-JsonPost "$base/$fondoId/contributions" '{"amount":200000}'
