# Pruebas de humo de la API del Portal Escolar.
# Uso:  .\scripts\test-api.ps1
# Requiere el backend en http://localhost:8080 y los datos de demostración.

param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$password = "Portal2026*"
$results = New-Object System.Collections.ArrayList

function Add-Result([string]$name, [bool]$ok, [string]$detail) {
    [void]$results.Add([pscustomobject]@{ Prueba = $name; Estado = $(if ($ok) { "OK" } else { "FALLO" }); Detalle = $detail })
    $color = if ($ok) { "Green" } else { "Red" }
    Write-Host ("[{0}] {1} - {2}" -f $(if ($ok) { "OK" } else { "FALLO" }), $name, $detail) -ForegroundColor $color
}

function Login([string]$email) {
    $body = @{ email = $email; password = $password } | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method Post `
        -ContentType "application/json" -Body $body
    return $response.accessToken
}

function Get-Api([string]$path, [string]$token) {
    return Invoke-RestMethod -Uri "$BaseUrl$path" -Method Get -Headers @{ Authorization = "Bearer $token" }
}

function Test-Api([string]$name, [string]$path, [string]$token) {
    try {
        $data = Get-Api $path $token
        $detail = if ($data -is [array]) { "$($data.Count) registros" } else { "respuesta recibida" }
        Add-Result $name $true $detail
        return $data
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        Add-Result $name $false "HTTP $status"
        return $null
    }
}

function Test-Forbidden([string]$name, [string]$path, [string]$token) {
    try {
        Get-Api $path $token | Out-Null
        Add-Result $name $false "Se permitió el acceso (debería ser 403)"
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        Add-Result $name ($status -eq 403) "HTTP $status"
    }
}

Write-Host "=== Endpoints públicos ===" -ForegroundColor Cyan
Test-Api "Institución pública" "/api/v1/public/institution" "" | Out-Null
Test-Api "Noticias públicas" "/api/v1/public/news" "" | Out-Null

Write-Host "`n=== Inicio de sesión ===" -ForegroundColor Cyan
$adminToken = Login "admin@portalescolar.edu.co"
Add-Result "Login ADMIN" ($null -ne $adminToken) "token recibido"
$teacherToken = Login "laura.gomez@portalescolar.edu.co"
Add-Result "Login DOCENTE" ($null -ne $teacherToken) "token recibido"
$studentToken = Login "sofia.mora@portalescolar.edu.co"
Add-Result "Login ESTUDIANTE" ($null -ne $studentToken) "token recibido"
$parentToken = Login "maria.giraldo@portalescolar.edu.co"
Add-Result "Login ACUDIENTE" ($null -ne $parentToken) "token recibido"

Write-Host "`n=== Panel administrativo ===" -ForegroundColor Cyan
Test-Api "Dashboard admin" "/api/v1/admin/dashboard" $adminToken | Out-Null
Test-Api "Listado estudiantes" "/api/v1/admin/students" $adminToken | Out-Null
Test-Api "Listado docentes" "/api/v1/admin/teachers" $adminToken | Out-Null
Test-Api "Listado acudientes" "/api/v1/admin/parents" $adminToken | Out-Null
Test-Api "Listado cursos" "/api/v1/admin/courses" $adminToken | Out-Null
Test-Api "Listado materias" "/api/v1/admin/subjects" $adminToken | Out-Null
Test-Api "Listado matrículas" "/api/v1/admin/enrollments" $adminToken | Out-Null
Test-Api "Noticias admin" "/api/v1/admin/news" $adminToken | Out-Null
Test-Api "Configuración institucional" "/api/v1/admin/settings/institution" $adminToken | Out-Null

Write-Host "`n=== Panel docente ===" -ForegroundColor Cyan
Test-Api "Dashboard docente" "/api/v1/teacher/dashboard" $teacherToken | Out-Null
Test-Api "Cursos docente" "/api/v1/teacher/courses" $teacherToken | Out-Null
$courseSubjects = Test-Api "Asignaturas docente" "/api/v1/teacher/course-subjects" $teacherToken
Test-Api "Tareas docente" "/api/v1/teacher/assignments" $teacherToken | Out-Null
Test-Api "Observaciones docente" "/api/v1/teacher/observations" $teacherToken | Out-Null
Test-Api "Materiales docente" "/api/v1/teacher/materials" $teacherToken | Out-Null
if ($courseSubjects -and $courseSubjects.Count -gt 0) {
    $csId = $courseSubjects[0].id
    Test-Api "Cuaderno de notas" "/api/v1/teacher/course-subjects/$csId/gradebook" $teacherToken | Out-Null
    Test-Api "Hoja de asistencia" "/api/v1/teacher/course-subjects/$csId/attendance" $teacherToken | Out-Null
}

Write-Host "`n=== Panel estudiante ===" -ForegroundColor Cyan
Test-Api "Dashboard estudiante" "/api/v1/student/dashboard" $studentToken | Out-Null
Test-Api "Perfil estudiante" "/api/v1/student/profile" $studentToken | Out-Null
Test-Api "Periodos" "/api/v1/student/periods" $studentToken | Out-Null
Test-Api "Calificaciones" "/api/v1/student/grades" $studentToken | Out-Null
Test-Api "Horario" "/api/v1/student/schedule" $studentToken | Out-Null
Test-Api "Tareas" "/api/v1/student/assignments" $studentToken | Out-Null
Test-Api "Materiales" "/api/v1/student/materials" $studentToken | Out-Null
Test-Api "Asistencia" "/api/v1/student/attendance" $studentToken | Out-Null
Test-Api "Observaciones" "/api/v1/student/observations" $studentToken | Out-Null
Test-Api "Notificaciones" "/api/v1/student/notifications" $studentToken | Out-Null

Write-Host "`n=== Panel acudiente ===" -ForegroundColor Cyan
Test-Api "Dashboard acudiente" "/api/v1/parent/dashboard" $parentToken | Out-Null
$children = Test-Api "Hijos autorizados" "/api/v1/parent/children" $parentToken
if ($children -and $children.Count -gt 0) {
    $childId = $children[0].studentId
    Test-Api "Resumen del hijo" "/api/v1/parent/children/$childId/overview" $parentToken | Out-Null
    Test-Api "Calificaciones del hijo" "/api/v1/parent/children/$childId/grades" $parentToken | Out-Null
    Test-Api "Asistencia del hijo" "/api/v1/parent/children/$childId/attendance" $parentToken | Out-Null
    Test-Api "Observaciones del hijo" "/api/v1/parent/children/$childId/observations" $parentToken | Out-Null
}
Test-Api "Reuniones" "/api/v1/parent/meetings" $parentToken | Out-Null

Write-Host "`n=== Control de acceso entre roles ===" -ForegroundColor Cyan
Test-Forbidden "Estudiante no accede a admin" "/api/v1/admin/dashboard" $studentToken
Test-Forbidden "Acudiente no accede a docente" "/api/v1/teacher/dashboard" $parentToken
Test-Forbidden "Docente no accede a estudiante" "/api/v1/student/dashboard" $teacherToken

$failed = $results | Where-Object { $_.Estado -ne "OK" }
Write-Host "`n=== Resumen ===" -ForegroundColor Cyan
Write-Host ("Total: {0}  Correctas: {1}  Fallidas: {2}" -f $results.Count, ($results.Count - $failed.Count), $failed.Count)
$results | Format-Table -AutoSize
if ($failed.Count -gt 0) { exit 1 }
