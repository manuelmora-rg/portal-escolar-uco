# Portal Escolar

Portal educativo institucional para centralizar la información y los servicios de un colegio:
sitio público, gestión académica y paneles privados para estudiantes, docentes, padres de familia
y administradores.

> **Estado actual:** Fase 1 (inspección del entorno) y Fase 2 (planificación) completadas.
> La implementación de código comienza tras la aprobación del plan.
>
> **Nombre del colegio:** pendiente de definir. Se usa "Portal Escolar" como nombre provisional.
> La identidad visual (escudo, colores, tipografía) está preparada para personalizarse.

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

### Comandos previstos (se validarán en la implementación)

```powershell
# Backend (desde backend/)
.\mvnw.cmd spring-boot:run

# Frontend (desde frontend/)
npm.cmd install
npm.cmd run dev
```

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

## 6. Documentación

- [`ARCHITECTURE.md`](./ARCHITECTURE.md): arquitectura, modelo de datos, permisos, API y mapa del sitio.
- [`PLAN.md`](./PLAN.md): fases, tareas y decisiones pendientes.
- [`CHANGELOG.md`](./CHANGELOG.md): historial de cambios.

---

## 7. Convenciones de trabajo

- Código y documentación en español; identificadores técnicos en inglés (consistente y estándar).
- Sin archivos gigantes: componentes y servicios con una responsabilidad clara.
- Sin lógica de negocio en componentes visuales: vive en hooks/servicios del frontend y en
  servicios del backend.
- Sin contraseñas en texto plano, sin SQL concatenado, sin datos académicos en endpoints públicos.
- Cada módulo se prueba antes de continuar. No se reporta como terminado lo que no funciona.
- Commits solo con cambios verificados. Ningún push a repositorios remotos sin autorización.

---

## 8. Privacidad y datos

- El repositorio no contendrá datos reales de estudiantes, docentes ni familias.
- Los datos de demostración serán ficticios y claramente identificables.
- Los archivos subidos por usuarios se almacenarán fuera del código fuente y se servirán con
  control de autorización.
