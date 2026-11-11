# Crea el rol y las bases de datos del proyecto en PostgreSQL.
# Requiere la contraseña del superusuario postgres.
# Uso:  .\scripts\db-setup.ps1 -PostgresPassword "tu_clave"
# Efecto: crea (si no existen) el rol portal_app y las bases portal_escolar y portal_escolar_test.
# No elimina ni sobrescribe nada existente.

param(
    [Parameter(Mandatory = $true)]
    [string]$PostgresPassword,
    [string]$DbUser = "portal_app",
    [string]$DbPassword = "portal_app_dev_2026"
)

$ErrorActionPreference = "Stop"
$psql = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
$createdb = "C:\Program Files\PostgreSQL\18\bin\createdb.exe"

$env:PGPASSWORD = $PostgresPassword

Write-Host "Verificando rol $DbUser..." -ForegroundColor Cyan
& $psql -U postgres -h localhost -d postgres -v ON_ERROR_STOP=1 -c "DO `$`$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='$DbUser') THEN CREATE ROLE $DbUser LOGIN PASSWORD '$DbPassword'; ELSE ALTER ROLE $DbUser WITH LOGIN PASSWORD '$DbPassword'; END IF; END `$`$;"

foreach ($db in @("portal_escolar", "portal_escolar_test")) {
    $exists = & $psql -U postgres -h localhost -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$db';"
    if ($exists.Trim() -ne "1") {
        Write-Host "Creando base $db..." -ForegroundColor Cyan
        & $createdb -U postgres -h localhost -O $DbUser -E UTF8 -T template0 $db
    } else {
        Write-Host "La base $db ya existe; no se modifica." -ForegroundColor Yellow
    }
}

Write-Host "Listo. Bases disponibles:" -ForegroundColor Green
& $psql -U postgres -h localhost -d postgres -c "SELECT datname, pg_get_userbyid(datdba) AS owner FROM pg_database WHERE datname LIKE 'portal%';"
