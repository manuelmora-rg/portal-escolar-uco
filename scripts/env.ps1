# Configura la sesión de PowerShell para el proyecto.
# No modifica variables globales ni el PATH del sistema.
# Uso:  . .\scripts\env.ps1

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
$env:Path = "$env:JAVA_HOME\bin;C:\Program Files\PostgreSQL\18\bin;$env:Path"

Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Cyan
Write-Host "Java:      $((& java -version 2>&1)[0])" -ForegroundColor Green
Write-Host "psql:      $((& psql --version))" -ForegroundColor Green
Write-Host "Sesión configurada. npm debe usarse como npm.cmd" -ForegroundColor Yellow
