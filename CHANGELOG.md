# Changelog

Todos los cambios importantes del proyecto se registran aquí.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).
El proyecto sigue [Versionado Semántico](https://semver.org/lang/es/).

## [Sin publicar]

### Añadido

#### Documentación y base del proyecto

- `README.md` con stack, entorno, requisitos, inicio rápido, credenciales demo y estado real.
- `ARCHITECTURE.md` con arquitectura, modelo de datos (32 tablas), matriz de permisos, mapa de
  API implementado, estructura del frontend, mapa del sitio, flujos, pruebas, despliegue y riesgos.
- `PLAN.md` con el avance por fases y las tareas pendientes declaradas explícitamente.
- `CHANGELOG.md` y `.gitignore`.
- Repositorio Git local en la rama `main`.
- `scripts/env.ps1` y `scripts/db-setup.ps1`.
- `scripts/test-api.ps1`: prueba de humo de la API con 39 casos.
- `API_DOCUMENTATION.md`: referencia completa de autenticación, usuarios, invitaciones, auditoría,
  endpoints por rol, contratos y ejemplos.

#### Identidad, seguridad y auditoría

- Migración `V4__invitaciones_auditoria.sql` con la tabla `invitation` (token hasheado, vigencia,
  uso único y revocación).
- Gestión de usuarios desde el panel admin: listar, consultar, crear, editar, activar/desactivar y
  restablecer contraseña.
- Invitaciones para crear cuentas sin registro público libre: token aleatorio mostrado una sola
  vez, guardado como hash SHA-256, vigencia de 7 días y revocación de invitaciones pendientes del
  mismo correo.
- Aceptación pública de invitación con creación del perfil correspondiente al rol.
- El rol es inmutable después de crear el usuario; desactivar una cuenta revoca sus refresh tokens.
- Protección para que un administrador no pueda desactivarse a sí mismo ni dejar el sistema sin
  administradores activos.
- Auditoría general en `audit_log` para usuarios, invitaciones, noticias, mensajes de contacto,
  configuración institucional y vínculos padre-hijo.
- Endpoints admin nuevos: `GET/POST /admin/users`, `GET/PUT /admin/users/{id}`,
  `PATCH /admin/users/{id}/status`, `POST /admin/users/{id}/reset-password`,
  `GET/POST /admin/invitations` y `GET /admin/audit-logs`.
- Endpoints públicos nuevos: `GET /auth/invitations/{token}` y `POST /auth/accept-invitation`.

#### Pruebas automatizadas

- Suite JUnit 5 + Spring Boot Test + MockMvc con **22 pruebas de integración** sobre
  `portal_escolar_test`:
  - autenticación, credenciales inválidas, 401 anónimo y refresh rotativo;
  - autorización cruzada entre los cuatro roles;
  - alta de docentes/estudiantes, desactivación, autodesactivación bloqueada y reset de contraseña;
  - invitación de un solo uso y auditoría de acciones administrativas;
  - aislamiento padre-hijo (IDOR) y acceso denegado a estudiantes no vinculados;
  - flujo académico completo: nota, corrección, asistencia, tarea y entrega.
- Perfil `test` con `application-test.yml` y datos demo controlados.
- Corrección de advertencias de validación en listas anidadas (`@Valid` sobre el tipo del elemento).

#### Backend

- Proyecto Spring Boot 4.1.2 con Java 21, Maven Wrapper y PostgreSQL 18.
- Migraciones Flyway `V1__esquema_inicial.sql` (31 tablas), `V2__datos_institucionales.sql` y
  `V3__agregar_version.sql`, con esquema `portal`.
- Seguridad: JWT de acceso (15 min) + refresh token rotativo en cookie HttpOnly
  `portal_refresh`, BCrypt strength 12, rate limit de login (5 intentos / 15 min),
  autorización por rol y validaciones por recurso.
- Autenticación: login, refresh, logout, `me` y cambio de contraseña.
- API pública: institución, oferta académica, noticias, detalle, eventos, galería y contacto.
- Panel estudiante: perfil, dashboard, periodos, calificaciones, horario, tareas con entrega,
  materiales, asistencia, observaciones y notificaciones.
- Panel docente: dashboard, periodos, cursos, libro de notas con creación/edición y auditoría,
  asistencia por lote, tareas, calificación de entregas, observaciones, materiales y notificaciones.
- Panel de padres: dashboard, periodos, hijos autorizados, resumen, calificaciones, horario,
  tareas, asistencia, observaciones, reuniones con confirmación y notificaciones.
- Panel admin: dashboard, estudiantes, docentes, padres, vínculos padre-hijo, cursos, materias,
  matrículas, noticias (crear/editar/publicar), mensajes de contacto, configuración institucional
  y notificaciones.
- Manejo global de errores con `ProblemDetail`, incluido `404` para recursos inexistentes.
- Datos de demostración ficticios solo en perfil `dev` (contraseña `Portal2026*`).
- Servido del SPA desde Spring Boot con fallback a `index.html` y recursos estáticos públicos.

#### Frontend

- Aplicación React 19 + Vite 8 + React Router 7 con Tailwind CSS 4 y tokens CSS.
- Modo claro/oscuro persistido, diseño responsive y componentes UI reutilizables.
- Cliente Axios con refresh automático del token y manejo de errores de API.
- Rutas protegidas por sesión y rol.
- Sitio público: inicio, institución, oferta académica, noticias y detalle, galería, calendario,
  contacto, login y página 404.
- Paneles de estudiante, docente, padre y administrador con datos reales de la API.
- Imágenes SVG institucionales de demostración.

### Corregido

- La auditoría usaba `audit_log` y `app_user` sin calificar el esquema `portal`, lo que abortaba
  transacciones de negocio; ahora usa `portal.audit_log` y `portal.app_user`.
- El matcher de rutas no-API usaba `getServletPath()`, lo que devolvía `403` en lugar de `401`
  para peticiones anónimas con MockMvc; ahora usa `getRequestURI()`.
- Los errores no controlados ahora se registran en el log del servidor sin exponer detalles al
  cliente.
- Las fechas sin hora (`YYYY-MM-DD`) se interpretaban con desfase por zona horaria; ahora se
  parsean como fecha local.
- El favicon y los recursos estáticos eran bloqueados por seguridad; se permitió su acceso sin
  abrir los endpoints de API.
- Un recurso inexistente devolvía `500`; ahora devuelve `404`.
- Las rutas del navegador no listadas devolvían `404`; se agregó fallback a `index.html` sin
  interceptar `/api/`, `/actuator/` ni archivos estáticos.

### Notas

- Verificado: compilación backend, `mvnw test` 22/22, `scripts/test-api.ps1` 39/39, build
  frontend y render de 33 rutas con Chrome headless sin errores de consola.
- Migraciones Flyway `V1`–`V4` aplicadas en `portal_escolar` y `portal_escolar_test`.
- Pendiente y declarado en `PLAN.md`: suite frontend (Vitest), accesibilidad formal, respaldos,
  monitoreo, despliegue y definición de nombre/identidad definitiva del colegio.
