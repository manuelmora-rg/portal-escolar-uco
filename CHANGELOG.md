# Changelog

Todos los cambios importantes del proyecto se registran aquí.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).
El proyecto sigue [Versionado Semántico](https://semver.org/lang/es/).

## [Sin publicar]

### Añadido

#### Documentación y base del proyecto

- `README.md` con stack, entorno, requisitos, inicio rápido, credenciales demo y estado real.
- `ARCHITECTURE.md` con arquitectura, modelo de datos (31 tablas), matriz de permisos, mapa de
  API implementado, estructura del frontend, mapa del sitio, flujos, pruebas, despliegue y riesgos.
- `PLAN.md` con el avance por fases y las tareas pendientes declaradas explícitamente.
- `CHANGELOG.md` y `.gitignore`.
- Repositorio Git local en la rama `main`.
- `scripts/env.ps1` y `scripts/db-setup.ps1`.
- `scripts/test-api.ps1`: prueba de humo de la API con 39 casos.

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

- Las fechas sin hora (`YYYY-MM-DD`) se interpretaban con desfase por zona horaria; ahora se
  parsean como fecha local.
- El favicon y los recursos estáticos eran bloqueados por seguridad; se permitió su acceso sin
  abrir los endpoints de API.
- Un recurso inexistente devolvía `500`; ahora devuelve `404`.
- Las rutas del navegador no listadas devolvían `404`; se agregó fallback a `index.html` sin
  interceptar `/api/`, `/actuator/` ni archivos estáticos.

### Notas

- Verificado: compilación backend, build frontend, `scripts/test-api.ps1` 39/39, render de
  33 rutas con Chrome headless sin errores de consola.
- Pendiente y declarado en `PLAN.md`: suites JUnit/Vitest, accesibilidad formal, respaldos,
  monitoreo, despliegue y definición de nombre/identidad definitiva del colegio.
