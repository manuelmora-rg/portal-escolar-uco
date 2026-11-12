# API REST — Portal Escolar

Documentación de la API implementada y verificada. Última actualización: **2026-11-11**.

- **Base URL local:** `http://localhost:8080`
- **Prefijo:** `/api/v1`
- **Formato:** JSON (`camelCase`), fechas ISO-8601, zona horaria `America/Bogota`
- **Autenticación:** JWT de acceso en `Authorization: Bearer <token>` + refresh token rotativo en cookie
- **Documento relacionado:** [`ARCHITECTURE.md`](./ARCHITECTURE.md) · [`README.md`](./README.md)

---

## 1. Convenciones

### 1.1 Encabezados

| Encabezado | Uso |
|------------|-----|
| `Content-Type: application/json` | Obligatorio en `POST`, `PUT` y `PATCH` con cuerpo JSON |
| `Authorization: Bearer <accessToken>` | Obligatorio en todos los endpoints privados |
| `Cookie: portal_refresh=<token>` | Enviada automáticamente por el navegador; solo aplica a `/api/v1/auth` |

### 1.2 Códigos de estado

| Código | Significado |
|--------|-------------|
| `200 OK` | Consulta o actualización correcta |
| `201 Created` | Recurso creado (usuario, invitación, cuenta aceptada) |
| `400 Bad Request` | Cuerpo inválido o error de validación de campos |
| `401 Unauthorized` | Sin sesión, token expirado o credenciales incorrectas |
| `403 Forbidden` | Sesión válida sin permiso para el recurso |
| `404 Not Found` | Recurso inexistente o invitación ya utilizada |
| `409 Conflict` | Correo, documento o código de empleado duplicado |
| `422 Unprocessable Entity` | Regla de negocio incumplida |
| `429 Too Many Requests` | Límite de intentos de login superado |
| `500 Internal Server Error` | Error no controlado (sin detalles internos al cliente) |

### 1.3 Formato de error

La API usa `ProblemDetail` de Spring. Ejemplo de error de validación:

```json
{
  "type": "about:blank",
  "title": "Datos inválidos",
  "status": 400,
  "detail": "Revisa los datos enviados.",
  "instance": "/api/v1/admin/users",
  "errors": [
    { "field": "email", "message": "El correo no es válido" }
  ]
}
```

Error de regla de negocio:

```json
{
  "type": "about:blank",
  "title": "Regla de negocio",
  "status": 422,
  "detail": "No puedes desactivar tu propia cuenta",
  "instance": "/api/v1/admin/users/3f1c.../status"
}
```

Los errores `500` nunca exponen trazas, SQL ni nombres de tablas. El detalle completo queda en el
log del servidor.

---

## 2. Autenticación

### 2.1 Flujo

1. `POST /auth/login` valida correo y contraseña (BCrypt 12).
2. Devuelve un **access token JWT** (15 min por defecto) en el cuerpo.
3. Establece la cookie `portal_refresh` (HttpOnly, `SameSite=Strict`, `Path=/api/v1/auth`) con el
   refresh token (7 días por defecto).
4. El frontend guarda el access token **solo en memoria** y lo envía en `Authorization`.
5. Al expirar, `POST /auth/refresh` rota el refresh token: revoca el anterior y emite uno nuevo.
6. Reutilizar un refresh token ya rotado devuelve `401`.
7. `POST /auth/logout` revoca el refresh token y borra la cookie.

El rate limit permite **5 intentos fallidos por correo + IP**; tras ello responde `429` durante
15 minutos.

### 2.2 `POST /auth/login`

Público. Inicia sesión.

**Solicitud**

```json
{
  "email": "admin@portalescolar.edu.co",
  "password": "Portal2026*"
}
```

**Respuesta `200`**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresInSeconds": 900,
  "user": {
    "id": "0b7d9c2e-...",
    "email": "admin@portalescolar.edu.co",
    "firstName": "Administrador",
    "lastName": "General",
    "fullName": "Administrador General",
    "role": "ADMIN",
    "mustChangePassword": false,
    "profileId": null
  }
}
```

**Errores:** `400` validación · `401` credenciales inválidas o cuenta desactivada · `429` bloqueo
temporal por intentos.

### 2.3 `POST /auth/refresh`

Público, requiere la cookie `portal_refresh`. No recibe cuerpo.

**Respuesta `200`:** mismo contrato que el login. Rota la cookie.

**Errores:** `401` si la cookie falta, expiró, fue revocada o la cuenta está desactivada.

### 2.4 `POST /auth/logout`

Público. Revoca el refresh token de la cookie y la elimina.

```json
{ "message": "Sesión cerrada" }
```

### 2.5 `GET /auth/me`

Requiere sesión. Devuelve el resumen del usuario autenticado (`UserSummary`).

```json
{
  "id": "0b7d9c2e-...",
  "email": "laura.gomez@portalescolar.edu.co",
  "firstName": "Laura",
  "lastName": "Gómez",
  "fullName": "Laura Gómez",
  "role": "TEACHER",
  "mustChangePassword": false,
  "profileId": "a1b2c3d4-..."
}
```

### 2.6 `POST /auth/change-password`

Requiere sesión. Cambia la contraseña, revoca todos los refresh tokens y borra la cookie.

**Solicitud**

```json
{
  "currentPassword": "Portal2026*",
  "newPassword": "NuevaClave2026*"
}
```

**Respuesta `200`**

```json
{ "message": "Contraseña actualizada. Inicia sesión de nuevo." }
```

**Errores:** `401` contraseña actual incorrecta · `400` nueva contraseña inválida (10–72
caracteres) · `422` si la nueva es igual a la actual.

---

## 3. Invitaciones (creación de cuentas)

No existe registro público libre. Un administrador invita por correo y el token se muestra **una
sola vez**; en la base de datos solo se guarda su hash SHA-256.

- Vigencia: **7 días**.
- Una invitación usada o revocada devuelve `404`.
- Crear una nueva invitación para un correo con invitación pendiente **revoca la anterior**.
- El rol queda fijado en la invitación y no puede cambiarse después.

### 3.1 `GET /auth/invitations/{token}`

Público. Valida una invitación y devuelve los datos mínimos para mostrar el formulario.

**Respuesta `200`**

```json
{
  "email": "nuevo.docente@example.test",
  "role": "TEACHER",
  "expiresAt": "2026-11-18T19:30:00Z",
  "valid": true
}
```

**Errores:** `404` si la invitación no existe, ya fue usada o fue revocada. Si existe pero está
vencida, responde `200` con `valid: false` para que el frontend muestre el mensaje adecuado.

### 3.2 `POST /auth/accept-invitation`

Público. Crea la cuenta asociada a la invitación.

**Solicitud**

```json
{
  "token": "q4Xb...token-mostrado-una-sola-vez",
  "firstName": "Nuevo",
  "lastName": "Docente",
  "password": "ClaveSegura2026*",
  "documentNumber": null,
  "phone": "3001234567",
  "employeeCode": "EMP-2026-042",
  "specialty": "Matemáticas"
}
```

Campos obligatorios según el rol:

| Rol | Campos obligatorios adicionales |
|-----|---------------------------------|
| `STUDENT` | `documentNumber` |
| `TEACHER` | `employeeCode` |
| `PARENT` | Ninguno |
| `ADMIN` | Ninguno |

**Respuesta `201`**

```json
{
  "userId": "d4e5f6a7-...",
  "email": "nuevo.docente@example.test",
  "role": "TEACHER",
  "message": "Cuenta activada correctamente. Ya puedes iniciar sesión."
}
```

**Errores:** `400` validación · `404` invitación inválida o ya usada · `409` correo ya registrado ·
`422` invitación expirada, ya utilizada o campos de perfil faltantes.

---

## 4. Administración de usuarios

Todos los endpoints requieren rol `ADMIN`.

### 4.1 `GET /admin/users`

Lista todas las cuentas ordenadas por rol y apellido.

```json
[
  {
    "id": "0b7d9c2e-...",
    "firstName": "Administrador",
    "lastName": "General",
    "fullName": "Administrador General",
    "email": "admin@portalescolar.edu.co",
    "role": "ADMIN",
    "enabled": true,
    "mustChangePassword": false,
    "lastLoginAt": "2026-11-11T19:10:00Z",
    "createdAt": "2026-11-10T12:00:00Z"
  }
]
```

### 4.2 `GET /admin/users/{userId}`

Devuelve un usuario. `404` si no existe.

### 4.3 `POST /admin/users`

Crea una cuenta directamente (alternativa a la invitación). Si no se envía
`temporaryPassword`, se genera una y se devuelve una sola vez.

**Solicitud**

```json
{
  "firstName": "Carlos",
  "lastName": "Ruiz",
  "email": "carlos.ruiz@example.test",
  "role": "TEACHER",
  "documentNumber": null,
  "phone": "3001234567",
  "employeeCode": "EMP-2026-043",
  "specialty": "Ciencias",
  "temporaryPassword": null
}
```

**Respuesta `201`**

```json
{
  "user": {
    "id": "9a8b7c6d-...",
    "firstName": "Carlos",
    "lastName": "Ruiz",
    "fullName": "Carlos Ruiz",
    "email": "carlos.ruiz@example.test",
    "role": "TEACHER",
    "enabled": true,
    "mustChangePassword": true,
    "lastLoginAt": null,
    "createdAt": "2026-11-11T19:30:00Z"
  },
  "temporaryPassword": "Xk3pQ9mN2vB7"
}
```

`temporaryPassword` es `null` si el administrador envió una contraseña explícita.

**Errores:** `409` correo/documento/código duplicado · `422` rol inválido o campos de perfil
faltantes.

### 4.4 `PUT /admin/users/{userId}`

Actualiza nombre, apellido y correo. El rol **no es editable**.

```json
{
  "firstName": "Carlos",
  "lastName": "Ruiz Gómez",
  "email": "carlos.ruiz@example.test"
}
```

**Errores:** `404` usuario inexistente · `409` correo ya usado por otra cuenta.

### 4.5 `PATCH /admin/users/{userId}/status`

Activa o desactiva una cuenta. Al desactivar, se revocan todos sus refresh tokens.

```json
{ "enabled": false }
```

**Respuesta `200`:** `AdminUserDto` actualizado.

**Errores:** `422` si el administrador intenta desactivarse a sí mismo o dejar el sistema sin
administradores activos.

### 4.6 `POST /admin/users/{userId}/reset-password`

Restablece la contraseña, marca `mustChangePassword = true` y revoca los refresh tokens.

**Solicitud** (contraseña explícita u omitida para generarla)

```json
{ "temporaryPassword": "Temporal2026*" }
```

**Respuesta `200`**

```json
{ "temporaryPassword": null }
```

Si se generó automáticamente, `temporaryPassword` contiene el valor mostrado una sola vez.

---

## 5. Invitaciones administrativas

### 5.1 `GET /admin/invitations`

Lista las invitaciones más recientes primero.

```json
[
  {
    "id": "5f4e3d2c-...",
    "email": "nuevo.docente@example.test",
    "role": "TEACHER",
    "expiresAt": "2026-11-18T19:30:00Z",
    "usedAt": null,
    "revokedAt": null,
    "createdAt": "2026-11-11T19:30:00Z",
    "createdByName": "Administrador General"
  }
]
```

### 5.2 `POST /admin/invitations`

Crea una invitación y devuelve el token **una sola vez**.

**Solicitud**

```json
{
  "email": "nuevo.docente@example.test",
  "role": "TEACHER"
}
```

**Respuesta `201`**

```json
{
  "invitation": {
    "id": "5f4e3d2c-...",
    "email": "nuevo.docente@example.test",
    "role": "TEACHER",
    "expiresAt": "2026-11-18T19:30:00Z",
    "usedAt": null,
    "revokedAt": null,
    "createdAt": "2026-11-11T19:30:00Z",
    "createdByName": "Administrador General"
  },
  "token": "q4Xb...token-mostrado-una-sola-vez"
}
```

**Errores:** `409` correo ya registrado · `422` rol inválido.

---

## 6. Auditoría

### 6.1 `GET /admin/audit-logs?limit=100`

Requiere rol `ADMIN`. Devuelve los registros más recientes. `limit` se limita al rango 1–500.

```json
[
  {
    "id": "7c6b5a49-...",
    "actorName": "Administrador General",
    "action": "USER_CREATED",
    "entity": "app_user",
    "entityId": "9a8b7c6d-...",
    "details": "{\"email\":\"carlos.ruiz@example.test\",\"role\":\"TEACHER\"}",
    "ip": "127.0.0.1",
    "createdAt": "2026-11-11T19:30:00Z"
  }
]
```

Acciones registradas actualmente:

| Acción | Origen |
|--------|--------|
| `USER_CREATED` | Alta de usuario por administrador |
| `USER_UPDATED` | Edición de nombre/correo |
| `USER_ENABLED` / `USER_DISABLED` | Cambio de estado |
| `USER_PASSWORD_RESET` | Restablecimiento administrativo |
| `INVITATION_CREATED` | Nueva invitación |
| `INVITATION_ACCEPTED` | Aceptación de invitación |
| `NEWS_CREATED` / `NEWS_UPDATED` / `NEWS_PUBLISHED` | Noticias |
| `CONTACT_STATUS_UPDATED` | Mensajes de contacto |
| `SETTINGS_UPDATED` | Configuración institucional |
| `PARENT_LINKED` | Vínculo padre-hijo |

La auditoría es de mejor esfuerzo: un fallo al registrar no revierte la operación de negocio.

---

## 7. API pública

Sin autenticación. Solo contenido publicado.

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/public/institution` | Nombre, escudo, colores, misión, visión e historia |
| `GET` | `/public/academic-offer` | Niveles, grados y materias |
| `GET` | `/public/news?page&size` | Noticias publicadas |
| `GET` | `/public/news/{slug}` | Detalle de noticia |
| `GET` | `/public/events?from&to` | Calendario publicado |
| `GET` | `/public/gallery` | Álbumes publicados |
| `POST` | `/public/contact` | Formulario de contacto |

**Ejemplo `POST /public/contact`**

```json
{
  "fullName": "Ana Pérez",
  "email": "ana@example.test",
  "phone": "3001234567",
  "subject": "Información de matrícula",
  "message": "Quisiera conocer los requisitos de admisión."
}
```

```json
{ "message": "Mensaje recibido. Gracias por contactarnos." }
```

---

## 8. Panel estudiante

Requiere rol `STUDENT`. El backend resuelve el estudiante desde el token; no se acepta un
`studentId` ajeno.

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/student/profile` | Perfil académico del estudiante |
| `GET` | `/student/dashboard` | Resumen de inicio |
| `GET` | `/student/periods` | Periodos académicos |
| `GET` | `/student/grades` | Calificaciones por materia |
| `GET` | `/student/schedule` | Horario |
| `GET` | `/student/assignments` | Tareas y estado de entrega |
| `POST` | `/student/assignments/{assignmentId}/submit` | Entregar tarea |
| `GET` | `/student/materials` | Materiales visibles |
| `GET` | `/student/attendance` | Asistencia |
| `GET` | `/student/observations` | Observaciones visibles |
| `GET` | `/student/notifications` | Notificaciones |
| `PATCH` | `/student/notifications/{notificationId}/read` | Marcar como leída |

**Ejemplo `POST /student/assignments/{assignmentId}/submit`**

```json
{
  "comment": "Entrego la actividad en el archivo adjunto.",
  "fileUrl": "https://example.test/entregas/tarea-1.pdf"
}
```

**Errores relevantes:** `403` si la tarea no pertenece a sus cursos · `422` si la tarea no está
publicada o cerrada.

---

## 9. Panel docente

Requiere rol `TEACHER`. Todas las operaciones se validan contra los cursos realmente asignados al
docente.

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/teacher/dashboard` | Resumen de inicio |
| `GET` | `/teacher/periods` | Periodos académicos |
| `GET` | `/teacher/courses` | Cursos asignados |
| `GET` | `/teacher/course-subjects` | Asignaturas que dicta |
| `GET` | `/teacher/courses/{courseId}/students` | Estudiantes del curso |
| `GET` | `/teacher/course-subjects/{id}/gradebook?periodId` | Libro de notas |
| `POST` | `/teacher/course-subjects/{id}/grades` | Registrar calificaciones por lote |
| `PUT` | `/teacher/grades/{gradeId}` | Corregir una calificación |
| `GET` | `/teacher/course-subjects/{id}/attendance?date` | Planilla de asistencia |
| `POST` | `/teacher/course-subjects/{id}/attendance` | Guardar asistencia por lote |
| `GET` | `/teacher/assignments` | Tareas creadas |
| `POST` | `/teacher/course-subjects/{id}/assignments` | Crear tarea |
| `GET` | `/teacher/assignments/{id}/submissions` | Entregas recibidas |
| `PUT` | `/teacher/submissions/{id}/grade` | Calificar entrega |
| `GET` | `/teacher/observations` | Observaciones registradas |
| `POST` | `/teacher/observations` | Crear observación |
| `GET` | `/teacher/materials` | Materiales publicados |
| `POST` | `/teacher/course-subjects/{id}/materials` | Publicar material |
| `GET` | `/teacher/notifications` | Notificaciones |
| `PATCH` | `/teacher/notifications/{id}/read` | Marcar como leída |

**Ejemplo `POST /teacher/course-subjects/{id}/grades`**

```json
{
  "entries": [
    {
      "enrollmentId": "b1c2d3e4-...",
      "periodId": "c1d2e3f4-...",
      "title": "Parcial 1",
      "type": "EXAM",
      "score": 4.5,
      "weight": 20.0,
      "comment": "Buen desempeño"
    }
  ]
}
```

**Ejemplo `POST /teacher/course-subjects/{id}/attendance`**

```json
{
  "date": "2026-11-11",
  "entries": [
    { "enrollmentId": "b1c2d3e4-...", "status": "PRESENT", "justification": null },
    { "enrollmentId": "b2c3d4e5-...", "status": "ABSENT", "justification": "Cita médica" }
  ]
}
```

**Ejemplo `POST /teacher/course-subjects/{id}/assignments`**

```json
{
  "title": "Taller de ecuaciones",
  "description": "Resolver los ejercicios 1 al 10.",
  "dueAt": "2026-11-18T23:59:00Z",
  "maxScore": 5.0,
  "attachmentUrl": null,
  "publish": true
}
```

**Errores relevantes:** `403` si el curso o la asignatura no pertenecen al docente · `404` recurso
inexistente · `400` nota fuera de 0.0–5.0 o peso fuera de 0.01–100.

---

## 10. Panel de padres

Requiere rol `PARENT`. El acceso a cada hijo se valida contra el vínculo `parent_student`
**autorizado**; un identificador ajeno devuelve `403` (protección IDOR).

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/parent/dashboard` | Resumen e hijos |
| `GET` | `/parent/periods` | Periodos académicos |
| `GET` | `/parent/children` | Hijos vinculados y autorizados |
| `GET` | `/parent/children/{studentId}/overview` | Resumen del hijo |
| `GET` | `/parent/children/{studentId}/grades?periodId` | Calificaciones |
| `GET` | `/parent/children/{studentId}/schedule` | Horario |
| `GET` | `/parent/children/{studentId}/assignments` | Tareas |
| `GET` | `/parent/children/{studentId}/attendance?from&to` | Asistencia |
| `GET` | `/parent/children/{studentId}/observations` | Observaciones visibles |
| `GET` | `/parent/meetings` | Reuniones |
| `POST` | `/parent/meetings/{meetingId}/confirm` | Confirmar reunión |
| `GET` | `/parent/notifications` | Notificaciones |
| `PATCH` | `/parent/notifications/{id}/read` | Marcar como leída |

**Ejemplo `POST /parent/meetings/{meetingId}/confirm`**

```json
{ "status": "CONFIRMED" }
```

Valores admitidos: `CONFIRMED` o `DECLINED`.

**Errores relevantes:** `403` si el estudiante no está vinculado o la reunión no corresponde a sus
hijos · `404` hijo o matrícula inexistente.

---

## 11. Panel administrativo (académico y contenido)

Requiere rol `ADMIN`.

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/admin/dashboard` | Indicadores generales |
| `GET` | `/admin/students` | Estudiantes con perfil y matrícula |
| `GET` | `/admin/teachers` | Docentes |
| `GET` | `/admin/parents` | Acudientes y número de hijos |
| `GET` | `/admin/courses` | Cursos |
| `GET` | `/admin/subjects` | Materias |
| `GET` | `/admin/enrollments` | Matrículas |
| `GET` | `/admin/news` | Noticias (incluidas no publicadas) |
| `POST` | `/admin/news` | Crear noticia |
| `PUT` | `/admin/news/{newsId}` | Editar noticia |
| `PATCH` | `/admin/news/{newsId}/publish` | Publicar noticia |
| `GET` | `/admin/contact-messages` | Mensajes de contacto |
| `PATCH` | `/admin/contact-messages/{id}/status` | Cambiar estado del mensaje |
| `GET` | `/admin/settings/institution` | Configuración institucional |
| `PUT` | `/admin/settings/institution` | Actualizar configuración |
| `POST` | `/admin/parent-links` | Vincular acudiente y estudiante |
| `GET` | `/admin/notifications` | Notificaciones |
| `PATCH` | `/admin/notifications/{id}/read` | Marcar como leída |

**Ejemplo `POST /admin/news`**

```json
{
  "title": "Jornada de puertas abiertas",
  "summary": "Invita a las familias a conocer la institución.",
  "content": "El próximo sábado realizaremos una jornada...",
  "imageUrl": "/images/noticias/puertas-abiertas.svg",
  "category": "Institucional",
  "publish": true
}
```

**Ejemplo `POST /admin/parent-links`**

```json
{
  "parentEmail": "maria.giraldo@portalescolar.edu.co",
  "studentDocument": "1020304050",
  "relationship": "Madre"
}
```

```json
{ "message": "Vínculo familiar registrado" }
```

**Ejemplo `PATCH /admin/contact-messages/{id}/status`**

```json
{ "status": "ANSWERED" }
```

**Ejemplo `PUT /admin/settings/institution`**

```json
{
  "name": "Portal Escolar",
  "shortName": "Portal Escolar",
  "slogan": "Educar para la vida",
  "mission": "Formar ciudadanos...",
  "vision": "Ser referentes en...",
  "history": "Fundada en...",
  "address": "Calle 00 #00-00",
  "phone": "3001234567",
  "email": "info@portalescolar.edu.co",
  "primaryColor": "#0b2545",
  "accentColor": "#38bdf8",
  "logoUrl": "/images/escudo.svg"
}
```

---

## 12. Matriz de permisos

| Recurso | ADMIN | TEACHER | STUDENT | PARENT |
|---------|:-----:|:-------:|:-------:|:------:|
| `/public/**` | Público | Público | Público | Público |
| `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/accept-invitation`, `/auth/invitations/{token}` | Público | Público | Público | Público |
| `/auth/me`, `/auth/change-password` | ✅ | ✅ | ✅ | ✅ |
| `/admin/**` | ✅ | ❌ | ❌ | ❌ |
| `/teacher/**` | ❌ | ✅ | ❌ | ❌ |
| `/student/**` | ❌ | ❌ | ✅ | ❌ |
| `/parent/**` | ❌ | ❌ | ❌ | ✅ |
| Datos de un estudiante concreto | ✅ | Solo sus cursos | Solo el propio | Solo hijos autorizados |

La autorización se aplica en dos capas: reglas de ruta/rol (`SecurityConfig` y
`@PreAuthorize`) y validación por recurso (`AccessGuard`). Ocultar un botón en el frontend no se
considera un control de seguridad.

---

## 13. Ejemplos de flujos completos

### 13.1 Invitar y activar una cuenta

```http
POST /api/v1/admin/invitations
Authorization: Bearer <adminToken>
Content-Type: application/json

{ "email": "nuevo.docente@example.test", "role": "TEACHER" }
```

```http
GET /api/v1/auth/invitations/<token>
```

```http
POST /api/v1/auth/accept-invitation
Content-Type: application/json

{
  "token": "<token>",
  "firstName": "Nuevo",
  "lastName": "Docente",
  "password": "ClaveSegura2026*",
  "employeeCode": "EMP-2026-042"
}
```

```http
POST /api/v1/auth/login
Content-Type: application/json

{ "email": "nuevo.docente@example.test", "password": "ClaveSegura2026*" }
```

### 13.2 Flujo académico verificado

1. Docente consulta `GET /teacher/course-subjects`.
2. Docente obtiene el libro de notas con `GET /teacher/course-subjects/{id}/gradebook`.
3. Docente registra notas con `POST /teacher/course-subjects/{id}/grades`.
4. Docente corrige una nota con `PUT /teacher/grades/{gradeId}`.
5. Docente guarda asistencia con `POST /teacher/course-subjects/{id}/attendance`.
6. Docente crea una tarea con `POST /teacher/course-subjects/{id}/assignments`.
7. Estudiante la entrega con `POST /student/assignments/{assignmentId}/submit`.
8. Docente revisa las entregas con `GET /teacher/assignments/{assignmentId}/submissions`.

Este flujo está cubierto por `AcademicFlowIntegrationTest`.

---

## 14. Verificación

| Prueba | Resultado |
|--------|-----------|
| Suite JUnit de integración (`mvnw test`) | 22/22 |
| Prueba de humo de API (`scripts/test-api.ps1`) | 39/39 |
| Render de rutas del frontend (Chrome headless) | 33/33 |
| Migraciones Flyway | `V1`–`V4` aplicadas |

Ver [`PLAN.md`](./PLAN.md) para el estado por fases y [`CHANGELOG.md`](./CHANGELOG.md) para el
historial de cambios.
