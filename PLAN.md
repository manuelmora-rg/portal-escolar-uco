# Plan de trabajo — Portal Escolar

Estado: **Fase 1 y Fase 2 completadas. Implementación pendiente de aprobación.**
Última actualización: 2026-11-10.

Leyenda: `[x]` completado y verificado · `[ ]` pendiente · `[~]` en curso · `[!]` bloqueado.

---

## Fase 1 — Inspección del entorno ✅

- [x] Verificar Java (Temurin 21.0.13, fuera del PATH).
- [x] Verificar Node.js (v26.13.0) y npm (11.20.0; `npm.ps1` bloqueado).
- [x] Verificar Maven (no instalado → se usará Maven Wrapper).
- [x] Verificar PostgreSQL (18.6, servicio en ejecución, credenciales pendientes).
- [x] Verificar Git (2.56.0; sin identidad configurada).
- [x] Verificar Docker (no instalado).
- [x] Identificar carpeta de trabajo: `C:\Users\morag\portal-escolar`.
- [x] Confirmar conectividad con Maven Central y registro npm.
- [x] Verificar espacio en disco (170 GB libres).

**Resultado:** entorno apto. Dos bloqueos menores: credenciales de PostgreSQL y configuración de
Maven (resuelta con wrapper).

---

## Fase 2 — Planificación ✅

- [x] Definir arquitectura general y capas.
- [x] Definir stack y versiones objetivo.
- [x] Diseñar modelo de datos inicial (31 tablas).
- [x] Definir roles y matriz de permisos.
- [x] Definir mapa del sitio público y de los cuatro paneles.
- [x] Definir estructura de carpetas frontend/backend.
- [x] Definir estrategia de seguridad y auditoría.
- [x] Definir convenciones de API.
- [x] Crear `README.md`, `ARCHITECTURE.md`, `PLAN.md`, `CHANGELOG.md`.
- [x] Inicializar repositorio Git local (sin commit ni remoto).
- [ ] **Aprobación del plan por el usuario.**

---

## Fase 3 — Diseño de interfaz

- [ ] Definir sistema de diseño (tokens, tipografía, espaciado, componentes base).
- [ ] Wireframes/prototipo de: inicio, login, panel estudiante, panel docente, panel padre,
      panel admin.
- [ ] Definir navegación lateral por rol y versión móvil.
- [ ] Definir modo claro/oscuro y personalización institucional.

**Entregable:** componentes UI base + layouts navegables con datos ficticios.

---

## Fase 4 — Backend

### 4.1 Base del proyecto

- [ ] Generar proyecto Spring Boot 4.1.x con Java 21 y Maven Wrapper.
- [ ] Configurar `application.yml` por perfiles y variables de entorno.
- [ ] Configurar Flyway y esquema `portal`.
- [ ] Crear migración `V1__esquema_inicial.sql`.
- [ ] Configurar OpenAPI (solo dev).
- [ ] Configurar manejo global de errores (`ProblemDetail`).
- [ ] Configurar CORS y cabeceras de seguridad.

### 4.2 Seguridad y usuarios

- [ ] Entidades `app_user`, perfiles y `refresh_token`.
- [ ] Registro/login/refresh/logout.
- [ ] BCrypt 12, política de contraseñas y bloqueo por intentos.
- [ ] `UserPrincipal`, `JwtService`, filtro JWT.
- [ ] `@PreAuthorize` por rol y `AccessGuard` por recurso.
- [ ] Auditoría de accesos y cambios.
- [ ] Pruebas de seguridad (acceso cruzado entre estudiantes, padres no autorizados).

### 4.3 Estructura académica

- [ ] Años, periodos, grados, cursos, materias, asignaciones, horarios.
- [ ] Matrículas y reglas de unicidad.
- [ ] CRUD administrativo + validaciones.
- [ ] Pruebas de servicios y controladores.

### 4.4 Calificaciones, tareas y asistencia

- [ ] Registro y corrección de notas con auditoría.
- [ ] Cálculo de promedios por materia/periodo (escala configurable).
- [ ] Tareas, entregas, archivos y retroalimentación.
- [ ] Asistencia diaria y justificaciones.
- [ ] Observaciones con visibilidad.
- [ ] Notificaciones a estudiantes y padres.
- [ ] Pruebas de reglas de negocio.

### 4.5 Contenido y comunicación

- [ ] Noticias, calendario, galería.
- [ ] Contacto público con rate limiting.
- [ ] Reuniones y confirmación de asistencia.
- [ ] Configuración institucional (`site_setting`).
- [ ] Reportes administrativos básicos.

### 4.6 Datos de demostración

- [ ] Seed ficticio: 1 admin, 2 docentes, 6 estudiantes, 4 padres, 2 cursos, 6 materias,
      notas, tareas y asistencia.
- [ ] Script de creación de base y usuario (`scripts/db-setup.ps1`).

---

## Fase 5 — Frontend

### 5.1 Base

- [ ] Proyecto Vite + React 19 + React Router 7.
- [ ] Tokens, estilos globales y modo claro/oscuro.
- [ ] `apiClient` con interceptores y refresh automático.
- [ ] Contextos de autenticación, tema y notificaciones.
- [ ] Rutas protegidas por sesión y rol.
- [ ] Componentes UI base y layouts.

### 5.2 Sitio público

- [ ] Inicio, institución (historia, misión, visión), oferta académica.
- [ ] Noticias y detalle, galería, calendario, contacto.
- [ ] Login y cambio de contraseña.
- [ ] Responsive y accesibilidad.

### 5.3 Paneles

- [ ] Panel estudiante completo.
- [ ] Panel docente completo.
- [ ] Panel de padres completo.
- [ ] Panel administrativo completo.
- [ ] Estados de carga, vacío y error.

---

## Fase 6 — Integración

- [ ] Conectar todos los servicios del frontend con la API real.
- [ ] Verificar los cuatro flujos de extremo a extremo.
- [ ] Ajustar CORS y cookies de refresh.
- [ ] Copiar build del frontend al backend y validar SPA fallback.
- [ ] Verificar subida/descarga de archivos con autorización.

---

## Fase 7 — Pruebas y correcciones

- [ ] Suite backend (unitarias + integración + seguridad).
- [ ] Suite frontend (Vitest + Testing Library).
- [ ] Pruebas manuales por rol en escritorio, tableta y móvil.
- [ ] Revisión de accesibilidad (teclado, contraste, etiquetas).
- [ ] Revisión de seguridad (IDOR, XSS, CSRF, rate limit, archivos).
- [ ] Corrección de defectos y regresión.

---

## Fase 8 — Preparación para producción

- [ ] Perfil `prod` y variables de entorno documentadas.
- [ ] Build único `.jar` con frontend incluido.
- [ ] Migraciones Flyway verificadas desde cero.
- [ ] Respaldo/restauración de PostgreSQL probados.
- [ ] Logs, monitoreo y rotación.
- [ ] Documentación de despliegue y operación.
- [ ] Revisión final de privacidad y datos personales.
- [ ] Autorización explícita antes de cualquier despliegue o publicación.

---

## Alcance del MVP (primera entrega funcional)

Para evitar un desarrollo interminable sin valor demostrable, la primera entrega incluye:

1. Sitio público con inicio, institución, noticias, calendario y contacto.
2. Login y control de acceso por roles.
3. Panel estudiante: calificaciones, horario, tareas, materiales, asistencia.
4. Panel docente: cursos, listas, calificaciones, tareas, asistencia, observaciones, materiales.
5. Panel de padres: hijos autorizados, calificaciones, asistencia, observaciones, comunicados.
6. Panel admin: estudiantes, docentes, padres, matrículas, cursos, materias, asignaciones,
   horarios, noticias, usuarios y configuración.
7. Seguridad, auditoría de notas, validaciones y pruebas de los flujos críticos.

Fuera del MVP (fase posterior): mensajería en tiempo real, videollamadas, pagos, app móvil nativa,
firma digital, integración con sistemas externos, analítica avanzada.

---

## Decisiones pendientes (bloquean o condicionan el inicio)

| # | Decisión | Propuesta por defecto | Impacto |
|---|----------|-----------------------|---------|
| D1 | Aprobación del plan y de la arquitectura | — | Bloquea Fase 3/4 |
| D2 | Nombre definitivo del colegio y escudo/colores | Placeholders "Portal Escolar" | Textos, marca, paquete Java |
| D3 | Credenciales de PostgreSQL o autorización para crear rol/base | Crear rol `portal_app` y bases `portal_escolar` / `portal_escolar_test` | Bloquea Fase 4 |
| D4 | Identidad de Git (`user.name`, `user.email`) | — | Bloquea commits |
| D5 | Escala de calificaciones y periodos | 0.0–5.0, 4 periodos | Reglas de negocio |
| D6 | Estilos: Tailwind CSS 4 + tokens vs. CSS Modules puro | Tailwind 4 + tokens CSS | Velocidad y mantenibilidad |
| D7 | Spring Boot 4.1.x vs. 3.5.x | 4.1.x estable | Compatibilidad |
| D8 | Uso de `npm.cmd` o cambio de ExecutionPolicy | `npm.cmd` (sin cambios globales) | Comandos de desarrollo |
| D9 | Agregar Java y PostgreSQL al PATH de usuario | Script local `scripts/env.ps1` | Comodidad |
| D10 | Orden de implementación: MVP completo vs. módulo por módulo | Módulo por módulo con demo al final de cada uno | Ritmo de revisión |

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

---

## Registro de avance

| Fecha | Fase | Detalle |
|-------|------|---------|
| 2026-11-10 | 1 | Inspección del entorno completada. |
| 2026-11-10 | 2 | Arquitectura, modelo de datos, permisos, mapa del sitio y documentos creados. |
| 2026-11-10 | 2 | Repositorio Git local inicializado. Sin commit (falta identidad). |
