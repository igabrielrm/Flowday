-- ============================================
-- INICIALIZACIÓN DE BASE DE DATOS
-- ============================================

-- Crear extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- DATOS POR DEFECTO
-- ============================================

-- 1. Crear usuario ADMIN (contraseña: admin123)
INSERT INTO usuarios (nombre, correo, contrasena, rol, estado, fecha_registro)
VALUES (
    'Administrador Sistema',
    'admin@uce.edu.ec',
    '$2a$10$NkM5p1qZ9YrP5x8R7w4nQeX3J9K7L2M4N6P8Q0R2T4V6W8X0Y2',
    'ADMIN',
    'ACTIVO',
    CURRENT_TIMESTAMP
) ON CONFLICT (correo) DO NOTHING;

-- 2. Crear usuario DEMO (contraseña: demo123)
INSERT INTO usuarios (nombre, correo, contrasena, rol, estado, carrera, fecha_registro)
VALUES (
    'Demo Estudiante',
    'demo@uce.edu.ec',
    '$2a$10$NkM5p1qZ9YrP5x8R7w4nQeX3J9K7L2M4N6P8Q0R2T4V6W8X0Y2',
    'ESTUDIANTE',
    'ACTIVO',
    'Ingeniería en Sistemas',
    CURRENT_TIMESTAMP
) ON CONFLICT (correo) DO NOTHING;

-- 3. Crear actividades de demostración
INSERT INTO actividades (usuario_id, titulo, descripcion, materia, tipo, fecha_inicio, hora_inicio, duracion_minutos, prioridad, estado)
SELECT 
    u.id,
    'Proyecto Demo',
    'Actividad de demostración para la casa abierta',
    'Programación',
    'DEBER',
    CURRENT_DATE,
    '10:00'::time,
    90,
    'ALTA',
    'PENDIENTE'
FROM usuarios u
WHERE u.correo = 'demo@uce.edu.ec'
ON CONFLICT DO NOTHING;

-- ============================================
-- VERIFICACIÓN
-- ============================================

SELECT '✅ Base de datos inicializada correctamente' AS mensaje;
SELECT '👥 Usuarios creados:' AS mensaje;
SELECT id, nombre, correo, rol FROM usuarios;