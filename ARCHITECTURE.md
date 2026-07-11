# Arquitectura — Event Organizer (transición a producción global)

## Visión

Producto de **productividad personal** (calendario, actividades, bienestar, comunidad, IA), desacoplado de instituciones. Objetivo: API REST + SPA + Docker en VPS.

## Decisiones del product owner

| Tema | Decisión |
|------|----------|
| Marca | 3 propuestas abajo — pendiente elección |
| Público | Productividad personal amplia |
| Carrera | Eliminar en Fase 2 |
| Auth | Email/contraseña + OAuth (fases) |
| Admin | URL oculta `/internal/login` (configurable) |
| Frontend | API + SPA (React/Vue) por fases |
| Deploy | Docker en VPS |
| i18n | ES + EN desde Fase 2 |
| Rol | `ESTUDIANTE` → `USER` en Fase 2 |
| HorarioClase | Renombrar a bloque recurrente en Fase 2 |
| IA prod | Solo Groq |

## Propuestas de nombre

1. **Flowday** — *Plan your day, own your flow.* — Paleta: `#2563EB`, `#0F172A`, `#F8FAFC`
2. **Chrona** — *Time that works for you.* — Paleta: `#7C3AED`, `#1E1B4B`, `#F5F3FF`
3. **Planweave** — *Weave tasks, calendar and focus.* — Paleta: `#059669`, `#064E3B`, `#ECFDF5`

## Fase 0 — Auditoría (resumen)

### Referencias UCE (~90+ archivos)

- **Java:** paquete `com.uce.*`, `UsuarioService.correoValido(@uce.edu.ec)`, seed admin legacy, prompts `IAService`, reportes admin
- **Templates:** títulos "Event Organizer UCE", `auth-common.html` topbar, `login.html`, `dashboard.html`, `community.html`, `admin-dashboard.html`
- **Static:** `uce-escudo.png`, `manifest.webmanifest`, `app.css`
- **Config:** `pom.xml` description, BD `eventorganizer_uce`

### Campo `carrera` (eliminar en Fase 2)

- `Usuario.java`, `registro-paso2.html`, `perfil.html`, `ComunidadService`, `UsuarioRepository`
- `AdminAnalyticsService`, `AgrupacionAcademicaService`, `AdminReportExportService`, `admin-analytics.js`
- Endpoints `/admin/api/analytics/*-por-carrera`

### Deuda de seguridad (priorizada)

| # | Riesgo | Estado Fase 1 |
|---|--------|----------------|
| 1 | `permitAll()` global | ✅ Reglas por rol |
| 2 | Admin público en login | ✅ `/internal/login` oculto |
| 3 | Credenciales hardcodeadas | ✅ Env vars `ADMIN_*` |
| 4 | AdminController sin auth | ✅ `AdminAuthHelper` en todos |
| 5 | CSRF deshabilitado | ✅ Cookie CSRF + forms |
| 6 | GET destructivos admin | ⏳ Fase 2 (migrar a POST) |
| 7 | APIs sin CSRF | ⏳ Ignoradas `/api/**` (JWT en Fase 3) |
| 8 | Secretos en `application.properties` | ⚠️ Mover a env (manual) |

### Mapa controllers → rutas

| Controller | Base | Auth Fase 1 |
|------------|------|-------------|
| AuthController | `/login`, `/registro`, `/logout` | Público |
| InternalAuthController | `/internal/login` | Público (solo login) |
| AdminController | `/admin/**` | ROLE_ADMIN |
| AdminReportController | `/admin/api/**` | ROLE_ADMIN |
| DashboardController | `/dashboard` | Autenticado |
| ActividadController | `/actividades/**` | Autenticado |
| CalendarioController | `/calendario/**` | Autenticado |
| HorarioController | `/horario/**` | Autenticado |
| ComunidadController | `/comunidad/**` | Autenticado |
| PerfilController | `/perfil/**` | Autenticado |
| IAController | `/api/ia/**` | Autenticado |
| BienestarController | `/api/bienestar/**` | Autenticado |
| NotificacionController | `/api/notificaciones/**` | Autenticado |

### Dependencias planificadas

| Fase | Dependencia |
|------|-------------|
| 3 | springdoc-openapi, validación DTOs |
| 4 | spring-boot-starter-websocket |
| 5 | spring-boot-starter-oauth2-client |
| 7 | flyway-core, spring-boot-starter-actuator |
| 7 opcional | Redis (pub/sub multi-instancia) |

## Fase 1 — Implementado

### Spring Security

- Rutas públicas explícitas; `/admin/**` requiere `ROLE_ADMIN`
- Sesión HTTP + `SecurityContext` sincronizado con `usuarioLogueado` (compatibilidad Thymeleaf)
- CSRF vía cookie (`XSRF-TOKEN`); APIs JSON exentas temporalmente
- Rate limit login: 15 intentos / 15 min por IP
- Headers: CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy, HSTS en `prod`
- Contraseña mínima: 8 caracteres

### Admin oculto

- Eliminado checkbox y botón "Acceso administrativo" de `login.html`
- Login admin: `GET/POST /internal/login` (`app.admin.path-prefix`)
- `robots.txt` bloquea `/admin/` y `/internal/`
- Seed admin solo si `app.admin.seed-enabled=true` + email/password en env

### Archivos nuevos

- `security/UsuarioPrincipal`, `SecurityUtils`, `LoginRateLimitFilter`, `SessionUsuarioSyncFilter`
- `config/AppProperties`
- `controller/InternalAuthController`
- `templates/internal-login.html`
- `static/robots.txt`

### Variables de entorno

```properties
app.admin.path-prefix=/internal
app.admin.seed-enabled=false
app.admin.seed-email=
app.admin.seed-password=
# prod: ADMIN_PATH_PREFIX, ADMIN_SEED_ENABLED, ADMIN_EMAIL, ADMIN_PASSWORD
```

## Roadmap y esfuerzo

| Fase | Contenido | Esfuerzo |
|------|-----------|----------|
| 0 | Auditoría + plan | S ✅ |
| 1 | Seguridad + admin oculto | M ✅ |
| 2 | Des-branding UCE, quitar carrera, i18n ES/EN, USER | L |
| 3 | API REST v1 + OpenAPI | L |
| 4 | WebSockets notificaciones | M |
| 5 | OAuth Google/Microsoft | M |
| 6 | Frontend SPA | XL |
| 7 | Docker + CI + Flyway + Actuator | M |

## Próximo paso

Fase 2 tras elegir nombre de marca. Confirmar **React vs Vue** para Fase 6.
