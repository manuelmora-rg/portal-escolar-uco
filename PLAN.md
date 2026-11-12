# Plan de trabajo — Portal Escolar

Estado: **MVP implementado y verificado, con identidad por invitación, auditoría y pruebas de integración.**
Fases 1–6 completadas; Fase 7 parcial (backend cubierto, frontend pendiente); Fase 8 pendiente.
Última actualización: 2026-11-11.

Leyenda: `[x]` completado y verificado · `[ ]` pendiente · `[~]` en curso · `[!]` bloqueado.

---

## Fase 1 — Inspección del entorno ✅

- [x] Verificar Java (Temurin 21.0.13, fuera del PATH).
- [x] Verificar Node.js (v26.13.0) y npm (11.20.0; `npm.ps1` bloqueado).
- [x] Verificar Maven (no instalado → se usó Maven Wrapper).
- [x] Verificar PostgreSQL (18.6, servicio en ejecución).
- [x] Verificar Git (2.56.0).
- [x] Verificar Docker (no instalado).
- [x] Confirmar carpeta de trabajo definitiva: `D:\portal-escolar`.
- [x] Confirmar conectividad con Maven Central y registro npm.
- [x] Verificar espacio en disco.

**Resultado:** entorno apto. Bloqueos resueltos con Maven Wrapper, `npm.cmd` y
`scripts/env.ps1` (sin modificar el PATH global).

---

## Fase 2 — Planificación ✅

- [x] Definir arquitectura general y capas.
- [x] Definir stack y versiones.
- [x] Diseñar modelo de datos (31 tablas).
- [x] Definir roles y matriz de permisos.
- [x] Definir mapa del sitio público y de los cuatro paneles.
- [x] Definir estructura de carpetas frontend/backend.
- [x] Definir estrategia de seguridad y auditoría.
- [x] Definir convenciones de API.
- [x] Crear `README.md`, `ARCHITECTURE.md`, `PLAN.md`, `CHANGELOG.md`.
- [x] Inicializar repositorio Git local en la rama `main`.
- [x] Aprobación del plan por el usuario (instrucción de continuar).

---

## Fase 3 — Diseño de interfaz ✅

- [x] Sistema de diseño: tokens CSS, tipografía, espaciado y componentes base.
- [x] Layouts navegables para sitio público y los cuatro paneles.
- [x] Navegación lateral por rol y versión móvil (menú colapsable).
- [x] Modo claro/oscuro persistido en `localStorage`.
- [x] Identidad institucional personalizable por tokens y configuración (`site_setting`).

**Entregable:** componentes UI base + layouts navegables con datos reales de la API.

---

## Fase 4 — Backend ✅

### 4.1 Base del proyecto

- [x] Proyecto Spring Boot 4.1.2 con Java 21 y Maven Wrapper.
- [x] `application.yml` por perfiles y variables de entorno.
- [x] Flyway y esquema `portal`.
- [x] Migración `V1__esquema_inicial.sql` (31 tablas) + `V2`, `V3` y `V4` (`invitation`).
- [ ] OpenAPI (no configurado; la API se documenta en `ARCHITECTURE.md`).
- [x] Manejo global de errores con `ProblemDetail` (incluye 404 de recursos).
- [x] CORS y cabeceras de seguridad.

### 4.2 Seguridad y usuarios

- [x] Entidades `app_user`, perfiles y refresh tokens.
- [x] Login/refresh/logout/me/change-password.
- [x] BCrypt 12, política de contraseñas y rate limit de login (5 intentos / 15 min).
- [x] `UserPrincipal`, `JwtService`, filtro JWT.
- [x] Autorización por rol y `AccessGuard` por recurso (padre-hijo, docente-curso).
- [x] Auditoría de cambios de calificaciones.
- [x] Pruebas de seguridad vía `scripts/test-api.ps1` (aislamiento entre roles con 403).

### 4.3 Estructura académica

- [x] Años, periodos, grados, cursos, materias, asignaciones y horarios.
- [x] Matrículas y reglas de unicidad.
- [x] Consulta administrativa y docente.
- [ ] CRUD administrativo completo de estructura académica (el MVP expone consulta).

### 4.4 Calificaciones, tareas y asistencia

- [x] Registro y corrección de notas con auditoría.
- [x] Promedios por materia/periodo (escala 0.0–5.0, 4 periodos).
- [x] Tareas, entregas y retroalimentación (sin carga de archivos).
- [x] Asistencia por lote y consulta.
- [x] Observaciones con visibilidad.
- [x] Notificaciones a estudiantes, padres y docentes.
- [x] Reglas de negocio en servicios.

### 4.5 Contenido y comunicación

- [x] Noticias, calendario, galería.
- [x] Contacto público con validación y rate limit.
- [x] Reuniones y confirmación de asistencia.
- [x] Configuración institucional (`site_setting`).
- [~] Reportes administrativos (dashboard con métricas; reportes exportables pendientes).

### 4.6 Datos de demostración

- [x] Seed ficticio: 13 usuarios, 6 estudiantes, 2 docentes, 4 padres, 2 cursos,
      6 materias, 144 notas, 32 tareas, 60 registros de asistencia.
- [x] `scripts/db-setup.ps1`.

---

## Fase 5 — Frontend ✅

### 5.1 Base

- [x] Vite 8 + React 19 + React Router 7.
- [x] Tokens, estilos globales y modo claro/oscuro.
- [x] `apiClient` con interceptores y refresh automático.
- [x] Contextos de autenticación y tema.
- [x] Rutas protegidas por sesión y rol.
- [x] Componentes UI base y layouts.

### 5.2 Sitio público

- [x] Inicio, institución (historia, misión, visión), oferta académica.
- [x] Noticias y detalle, galería, calendario, contacto.
- [x] Login y cambio de contraseña.
- [x] Responsive.
- [~] Revisión formal de accesibilidad pendiente.

### 5.3 Paneles

- [x] Panel estudiante completo.
- [x] Panel docente completo.
- [x] Panel de padres completo.
- [x] Panel administrativo completo.
- [x] Estados de carga, vacío y error.

---

## Fase 6 — Integración ✅

- [x] Todos los servicios del frontend conectados a la API real.
- [x] Cuatro flujos de extremo a extremo verificados con Chrome headless.
- [x] CORS y cookie de refresh ajustados.
- [x] Build del frontend copiado al backend y SPA fallback validado.
- [ ] Subida/descarga de archivos (fuera del MVP actual).

---

## Fase 7 — Pruebas y correcciones [~]

- [x] Suite backend formal (JUnit 5 + MockMvc): 22 pruebas de integración sobre `portal_escolar_test`.
- [ ] Suite frontend (Vitest + Testing Library): pendiente.
- [x] Verificación de render de 33 rutas (público + 4 roles) sin errores de consola.
- [x] Prueba de humo de API: 39/39 casos, incluidos 403 de aislamiento.
- [x] Pruebas de autenticación: login, credenciales inválidas, 401 anónimo, refresh rotativo.
- [x] Pruebas de autorización: 403 entre los cuatro roles.
- [x] Pruebas de identidad: alta de usuarios, desactivación, autodesactivación bloqueada, reset de contraseña.
- [x] Pruebas de invitaciones: token de un solo uso, información pública y aceptación.
- [x] Pruebas de auditoría: registro de acciones administrativas.
- [x] Pruebas de aislamiento padre-hijo (IDOR): acceso a hijos vinculados y 403 a ajenos.
- [x] Prueba de flujo académico: nota, corrección, asistencia, tarea y entrega.
- [~] Pruebas manuales responsive: revisión básica; falta tableta/móvil real.
- [ ] Revisión de accesibilidad (teclado, contraste, etiquetas).
- [~] Revisión de seguridad: IDOR y roles cubiertos; falta pentest formal.
- [x] Corrección de defectos encontrados (zona horaria de fechas, favicon, 404, fallback SPA).

---

## Fase 8 — Preparación para producción [~]

- [x] Perfil `prod` y variables de entorno documentadas.
- [x] Build único con frontend incluido (servido por Spring Boot).
- [x] Migraciones Flyway verificadas en base local.
- [ ] Respaldo/restauración de PostgreSQL probados.
- [ ] Logs, monitoreo y rotación.
- [ ] Documentación de despliegue y operación.
- [~] Revisión de privacidad y datos personales (datos demo ficticios).
- [ ] Autorización explícita antes de cualquier despliegue o publicación.

---

## Alcance del MVP (primera entrega funcional)

1. Sitio público con inicio, institución, noticias, calendario y contacto. ✅
2. Login y control de acceso por roles. ✅
3. Panel estudiante: calificaciones, horario, tareas, materiales, asistencia. ✅
4. Panel docente: cursos, listas, calificaciones, tareas, asistencia, observaciones, materiales. ✅
5. Panel de padres: hijos autorizados, calificaciones, asistencia, observaciones, reuniones. ✅
6. Panel admin: estudiantes, docentes, padres, cursos, materias, noticias, mensajes y configuración. ✅
7. Seguridad, auditoría de notas, validaciones y pruebas de los flujos críticos. ✅

Fuera del MVP (fase posterior): mensajería en tiempo real, videollamadas, pagos, app móvil nativa,
firma digital, integración con sistemas externos, analítica avanzada, carga de archivos.

---

## Decisiones pendientes

| # | Decisión | Propuesta por defecto | Impacto |
|---|----------|-----------------------|---------|
| D1 | Aprobación del plan y de la arquitectura | Aprobado para el MVP | — |
| D2 | Nombre definitivo del colegio y escudo/colores | Placeholders "Portal Escolar" | Textos, marca, paquete Java |
| D3 | Credenciales de PostgreSQL | Rol `portal_app`, bases `portal_escolar` / `portal_escolar_test` | Resuelto en local |
| D4 | Identidad de Git | Configurada localmente | Commits |
| D5 | Escala de calificaciones y periodos | 0.0–5.0, 4 periodos | Reglas de negocio |
| D6 | Estilos | Tailwind CSS 4 + tokens CSS | Implementado |
| D7 | Spring Boot 4.1.x vs. 3.5.x | 4.1.2 | Implementado |
| D8 | Uso de `npm.cmd` | Sin cambios globales | Implementado |
| D9 | Java y PostgreSQL en el PATH | Script local `scripts/env.ps1` | Implementado |
| D10 | Orden de implementación | MVP completo | Implementado |

---

## Criterios de aceptación por módulo

Un módulo se considera terminado cuando:

1. Compila sin errores ni advertencias críticas.
2. Sus pruebas automatizadas pasan.
3. El flujo funciona de extremo a extremo con datos ficticios.
4. Los permisos se validan en el backend con pruebas de acceso denegado.
5. No expone datos personales en endpoints públicos ni en logs.
6. Está documentado en `CHANGELOG.md` y, si aplica, en `ARCHITECTURE.md`.
7. Se verificó en escritorio y en ancho móvil.

> Nota honesta: el MVP cumple 1, 3, 4, 5, 6 y verificación de escritorio. La suite backend
> automatizada (punto 2) ya está cubierta con 22 pruebas de integración; la suite frontend y la
> verificación en dispositivos móviles reales (punto 7) siguen pendientes y se registran como tales.

---

## Registro de avance

| Fecha | Fase | Detalle |
|-------|------|---------|
| 2026-11-10 | 1 | Inspección del entorno completada. |
| 2026-11-10 | 2 | Arquitectura, modelo de datos, permisos, mapa del sitio y documentos creados. |
| 2026-11-10 | 2 | Repositorio Git local inicializado. |
| 2026-11-10 | 3 | Sistema de diseño, layouts y modo claro/oscuro implementados. |
| 2026-11-10 | 4 | Backend completo: seguridad, módulos público/estudiante/docente/padre/admin, datos demo. |
| 2026-11-10 | 5 | Frontend completo: sitio público y cuatro paneles. |
| 2026-11-10 | 6 | Integración verificada; SPA servida por Spring Boot. |
| 2026-11-10 | 7 | Prueba de humo 39/39 y render de 33 rutas sin errores de consola. |
| 2026-11-11 | 7 | Identidad por invitación, auditoría general y 22 pruebas JUnit de integración (22/22). |
| 2026-11-11 | 7 | Migración `V4` aplicada en desarrollo; `API_DOCUMENTATION.md` publicado. |
