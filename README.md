# Portal Escolar

Portal educativo institucional para centralizar la información y los servicios de un colegio:
sitio público, gestión académica y paneles privados para estudiantes, docentes, padres de familia
y administradores.

> **Estado actual:** MVP implementado y verificado de extremo a extremo (backend + frontend,
> cuatro roles, sitio público y paneles privados), con gestión de identidad por invitación,
> auditoría general y suite de integración JUnit (22/22). Ver "Estado de implementación" más abajo.
>
> **Nombre del colegio:** pendiente de definir. Se usa "Portal Escolar" como nombre provisional.
> La identidad visual usa una paleta de verde oscuro, verde principal, verdes claros, blanco y grises,
> centralizada en `frontend/src/index.css` (tokens `--color-brand-*` y `--color-accent-*`) y adaptada
> a modo claro y oscuro.
>
> **Datos de demostración:** solo se cargan en el perfil `dev` y son ficticios.

---

## 1. Stack tecnológico

| Capa | Tecnología | Versión objetivo |
|------|------------|------------------|
| Frontend | React + JavaScript (JSX) | React 19.x |
| Build frontend | Vite | Vite 8.x |
| Routing | React Router | 7.x |
| Estilos | CSS3 con design tokens (+ Tailwind CSS 4 opcional, ver decisiones) | — |
| Backend | Java | 21 (LTS) |
| Framework | Spring Boot (Web, Security, Data JPA, Validation) | 4.1.x estable |
| Build backend | Maven (Maven Wrapper incluido en el repo) | 3.9.x |
| Base de datos | PostgreSQL | 18.x |
| Migraciones | Flyway | versión gestionada por Spring Boot |
| Auth | JWT de acceso + refresh token en cookie HttpOnly | — |
| Hash de contraseñas | BCrypt (strength 12) | — |
| Testing back | JUnit 5, Mockito, Spring Boot Test | — |
| Testing front | Vitest + React Testing Library | — |
| Control de versiones | Git | 2.56 |

---

## 2. Entorno detectado (Fase 1)

Fecha de inspección: 2026-11-10. Equipo: Windows 11 Pro (10.0.26300), PowerShell 5.1.

| Herramienta | Detectada | Ubicación | Estado |
|-------------|-----------|-----------|--------|
| Java (Temurin) | 21.0.13+9 LTS | `C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot` | Instalada, **no está en el PATH** |
| Node.js | v26.13.0 | `C:\Program Files\nodejs\node.exe` | OK |
| npm | 11.20.0 | `C:\Program Files\nodejs\npm.cmd` | OK, pero `npm.ps1` bloqueado por la política de ejecución de PowerShell |
| Maven | No instalada | — | Se usará **Maven Wrapper** dentro del repositorio (no requiere instalación global) |
| PostgreSQL | 18.6 | `C:\Program Files\PostgreSQL\18` | Servicio `postgresql-x64-18` **en ejecución**; falta definir credenciales |
| Git | 2.56.0.windows.4 | `C:\Program Files\Git\cmd\git.exe` | OK; **sin `user.name` ni `user.email` configurados** |
| Docker | No instalado | — | Los tests de integración usarán una base PostgreSQL local, no Testcontainers |
| IDE | VS Code (carpeta `.vscode` presente) | — | OK |

### Notas del entorno

- **Java y `psql` no están en el PATH.** No se modificará el PATH global sin autorización.
  Se incluirá `scripts/env.ps1` para configurar la sesión de trabajo de forma local y reversible.
- **`npm` en PowerShell:** `npm.ps1` está bloqueado por `ExecutionPolicy`. Mientras no se autorice
  cambiar la política, los comandos se ejecutan con `npm.cmd` o `cmd /c npm ...`.
- **Maven:** el proyecto incluirá `mvnw` / `mvnw.cmd` y `.mvn/wrapper`, que descargan Maven a la
  caché del usuario. No se instala nada a nivel global.
- **PostgreSQL:** el servidor responde en `localhost:5432`, pero la contraseña del usuario
  `postgres` no es conocida por el agente. Se necesita la credencial (o autorización para crear
  un rol/base de datos) antes de la Fase 4.
- **Git:** no hay identidad global. Se pedirá `user.name` y `user.email` antes del primer commit.

---

## 3. Requisitos para ejecutar el proyecto

- Java 21 (JDK).
- Node.js 20 o superior y npm.
- PostgreSQL 16 o superior en ejecución.
- Git.

No se requiere instalar Maven: el wrapper lo resuelve.

### Variables de entorno

El backend leerá configuración desde variables de entorno (nunca credenciales en el repositorio).
Se incluirá `backend/.env.example` y `frontend/.env.example` como plantillas.

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/portal_escolar` |
| `DB_USERNAME` | Usuario de la aplicación | `portal_app` |
| `DB_PASSWORD` | Contraseña del usuario | *(secreta)* |
| `JWT_SECRET` | Clave HMAC de al menos 256 bits | *(secreta, generada)* |
| `JWT_ACCESS_MINUTES` | Duración del access token | `15` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos del frontend | `http://localhost:5173` |
| `APP_UPLOAD_DIR` | Carpeta de archivos subidos | `./uploads` |
| `VITE_API_URL` | URL base de la API para el frontend | `http://localhost:8080/api/v1` |

### Inicio rápido (verificado en este equipo)

```powershell
# 1. Configurar la sesión (Java y psql fuera del PATH)
. .\scripts\env.ps1

# 2. Crear rol y bases de datos (una sola vez; requiere el usuario postgres)
.\scripts\db-setup.ps1

# 3. Backend en http://localhost:8080 (aplica Flyway y carga datos demo en dev)
cd backend
.\mvnw.cmd spring-boot:run

# 4. Frontend en desarrollo en http://localhost:5173 (otra terminal)
cd frontend
npm.cmd install
npm.cmd run dev
```

Para servir todo desde un solo origen (recomendado para probar el build real):

```powershell
cd frontend
npm.cmd run build          # genera el build en backend/src/main/resources/static

cd ..\backend
.\mvnw.cmd -q -DskipTests process-resources
.\mvnw.cmd spring-boot:run # abre http://localhost:8080
```

### Credenciales de demostración

Contraseña para todas las cuentas demo: `Portal2026*` (solo perfil `dev`).

| Rol | Correo |
|-----|--------|
| ADMIN | `admin@portalescolar.edu.co` |
| TEACHER | `laura.gomez@portalescolar.edu.co` |
| TEACHER | `carlos.ruiz@portalescolar.edu.co` |
| STUDENT | `sofia.mora@portalescolar.edu.co` |
| PARENT | `maria.giraldo@portalescolar.edu.co` |

### Pruebas

```powershell
# Suite de integración backend (22 pruebas; usa portal_escolar_test)
cd backend
.\mvnw.cmd test

# Prueba de humo de la API contra el backend en ejecución (39 casos)
.\scripts\test-api.ps1
```

La suite JUnit cubre autenticación y refresh rotativo, autorización por rol, gestión de usuarios,
invitaciones de un solo uso, restablecimiento de contraseña, auditoría, aislamiento padre-hijo
(IDOR) y el flujo académico completo (notas, asistencia, tareas y entregas).

---

## 4. Estructura del repositorio

```text
portal-escolar/
├─ frontend/                 # Aplicación React + Vite
├─ backend/                  # API REST Spring Boot + Maven
├─ docs/                     # Diagramas y documentación técnica ampliada
├─ scripts/                  # Utilidades locales (entorno, base de datos)
├─ README.md
├─ ARCHITECTURE.md
├─ API_DOCUMENTATION.md
├─ PLAN.md
├─ CHANGELOG.md
└─ .gitignore
```

La estructura interna detallada de `frontend/` y `backend/` está en `ARCHITECTURE.md`.

---

## 5. Roles del sistema

| Rol | Descripción |
|-----|-------------|
| `ADMIN` | Administra usuarios, matrículas, estructura académica, contenido y configuración. |
| `TEACHER` | Gestiona sus cursos, calificaciones, tareas, asistencia, observaciones y materiales. |
| `STUDENT` | Consulta su información académica y entrega trabajos. |
| `PARENT` | Consulta únicamente la información de los hijos asociados y autorizados. |

Los permisos se validan siempre en el backend. Ocultar un botón en el frontend no es un control
de seguridad.

---

## 6. Estado de implementación

| Área | Estado | Detalle |
|------|--------|---------|
| Base de datos | ✅ | 32 tablas en el esquema `portal` (31 + `invitation`), migraciones Flyway `V1`–`V4`, datos demo en `dev`. |
| Seguridad | ✅ | JWT de acceso + refresh rotativo en cookie HttpOnly, BCrypt 12, rate limit de login, autorización por rol y por recurso. |
| Identidad | ✅ | API de cuentas por invitación del administrador (sin registro público), invitaciones de un solo uso con token hasheado, activación/desactivación, restablecimiento de contraseña y rol inmutable. Interfaz web pendiente. |
| API pública | ✅ | Institución, oferta académica, noticias, eventos, galería y contacto. |
| Panel estudiante | ✅ | Inicio, calificaciones, horario, tareas con entrega, materiales, asistencia, observaciones, notificaciones. |
| Panel docente | ✅ | Inicio, cursos, libro de notas (crear/editar), asistencia por lote, tareas y calificación de entregas, observaciones, materiales. |
| Panel de padres | ✅ | Hijos autorizados, resumen, calificaciones por periodo, horario, tareas, asistencia, observaciones, reuniones. |
| Panel admin | ✅ | Inicio, estudiantes, docentes, padres y vínculos, cursos, materias, matrículas, noticias (CRUD/publicación), mensajes y configuración institucional. |
| API admin de identidad | ✅ | Usuarios, invitaciones y auditoría expuestos por la API; falta su interfaz en el panel web. |
| Auditoría | ✅ | Registro general en `audit_log` (usuarios, invitaciones, noticias, mensajes, configuración y vínculos) y auditoría de cambios de calificaciones. |
| Pruebas backend | ✅ | 22 pruebas JUnit de integración sobre `portal_escolar_test`: autenticación, roles, IDOR, usuarios, invitaciones, auditoría y flujo académico. |
| SPA servida por Spring Boot | ✅ | Build de Vite copiado a `static`, fallback a `index.html`, recursos estáticos públicos. |
| Verificación | ✅ | `mvnw test` (22/22), `scripts/test-api.ps1` (39/39), compilación backend, build frontend y render de 33 rutas con Chrome headless sin errores de consola. |

Pendiente para producción: nombre/identidad definitiva del colegio, interfaz web de usuarios,
invitaciones y auditoría (la API ya existe), suite frontend (Vitest + Testing Library), revisión de
accesibilidad, respaldos y monitoreo. Ver `PLAN.md`.

---

## 7. Documentación

- [`ARCHITECTURE.md`](./ARCHITECTURE.md): arquitectura, modelo de datos, permisos, API y mapa del sitio.
- [`API_DOCUMENTATION.md`](./API_DOCUMENTATION.md): referencia completa de endpoints, contratos y ejemplos.
- [`PLAN.md`](./PLAN.md): fases, tareas y decisiones pendientes.
- [`CHANGELOG.md`](./CHANGELOG.md): historial de cambios.

---

## 8. Convenciones de trabajo

- Código y documentación en español; identificadores técnicos en inglés (consistente y estándar).
- Sin archivos gigantes: componentes y servicios con una responsabilidad clara.
- Sin lógica de negocio en componentes visuales: vive en hooks/servicios del frontend y en
  servicios del backend.
- Sin contraseñas en texto plano, sin SQL concatenado, sin datos académicos en endpoints públicos.
- Cada módulo se prueba antes de continuar. No se reporta como terminado lo que no funciona.
- Commits solo con cambios verificados. Ningún push a repositorios remotos sin autorización.

---

## 9. Privacidad y datos

- El repositorio no contendrá datos reales de estudiantes, docentes ni familias.
- Los datos de demostración serán ficticios y claramente identificables.
- Los archivos subidos por usuarios se almacenarán fuera del código fuente y se servirán con
  control de autorización.
