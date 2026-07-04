# 📝 Changelog - Event Organizer UCE

---

## [1.0.0] - 2026-06-27

### 🚀 Añadido

- **Autenticación y registro:**
  - Registro en 3 pasos con validación de correo @uce.edu.ec
  - Login con BCrypt y sesiones seguras
  - Roles ADMIN y ESTUDIANTE

- **Gestión de actividades:**
  - CRUD completo de actividades
  - Validación de conflictos de horario
  - Filtros por estado y prioridad

- **Dashboard:**
  - Resumen del día (racha, tareas, horas, Pomodoro)
  - Lista de actividades de hoy
  - Semáforo académico por materia
  - Ranking de estudiantes

- **Mascota interactiva:**
  - Cambia de estado según carga de trabajo
  - Consejos personalizados al hacer clic

- **Pomodoro:**
  - Modos: Estudio, Pausa, Descanso
  - Pausas activas con opciones reales
  - Guardado automático de sesiones

- **IA y Asistente:**
  - Reagendamiento inteligente
  - Modo auxilio de estudio
  - Detección de bloqueo creativo

- **Comunidad:**
  - Búsqueda de compañeros por nombre/carrera
  - Compatibilidad basada en materias

- **Calendario:**
  - Vista mensual con colores según prioridad

- **Panel de Administración:**
  - Gestión de usuarios
  - Publicación de anuncios globales
  - Estadísticas del sistema

- **Seguridad:**
  - BCrypt para encriptación de contraseñas
  - Sesiones seguras
  - Protección de rutas por rol

- **Documentación:**
  - README.md
  - Manual de usuario
  - Guía de demo
  - Changelog

---

## [0.1.0] - 2026-06-20

### 🚀 Añadido

- Estructura inicial del proyecto con Spring Boot 3.3.4
- Configuración de base de datos PostgreSQL
- Modelos: Usuario, Actividad, RegistroBienestar, Anuncio
- Repositorios JPA
- Servicios básicos