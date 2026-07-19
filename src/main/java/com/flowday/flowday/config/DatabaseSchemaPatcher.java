package com.flowday.flowday.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Aplica parches de esquema cuando ddl-auto no actualiza tablas existentes en PostgreSQL.
 * Debe ejecutarse temprano (@PostConstruct) para que schedulers y APIs no fallen
 * por columnas nuevas como {@code version}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseSchemaPatcher {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaPatcher.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DatabaseSchemaPatcher(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void aplicarParches() {
        if (!esPostgreSQL()) {
            return;
        }
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS reagendamiento_log (
                    id BIGSERIAL PRIMARY KEY,
                    actividad_id BIGINT,
                    usuario_id BIGINT NOT NULL,
                    fecha_anterior DATE,
                    hora_anterior TIME,
                    fecha_nueva DATE,
                    hora_nueva TIME,
                    motivo VARCHAR(500),
                    conflicto_con_id BIGINT,
                    conflicto_con_tipo VARCHAR(50),
                    exitoso BOOLEAN DEFAULT FALSE,
                    automatico BOOLEAN DEFAULT TRUE,
                    fecha_ejecucion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            agregarColumnaSiFalta("reagendamiento_log", "automatico", "BOOLEAN DEFAULT TRUE");
            agregarColumnaSiFalta("reagendamiento_log", "exitoso", "BOOLEAN DEFAULT FALSE");
            agregarColumnaSiFalta("reagendamiento_log", "fecha_ejecucion", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            agregarColumnaSiFalta("reagendamiento_log", "conflicto_con_id", "BIGINT");
            agregarColumnaSiFalta("reagendamiento_log", "conflicto_con_tipo", "VARCHAR(50)");
            agregarColumnaSiFalta("reagendamiento_log", "motivo", "VARCHAR(500)");
            agregarColumnaSiFalta("reagendamiento_log", "fecha_anterior", "DATE");
            agregarColumnaSiFalta("reagendamiento_log", "hora_anterior", "TIME");
            agregarColumnaSiFalta("reagendamiento_log", "fecha_nueva", "DATE");
            agregarColumnaSiFalta("reagendamiento_log", "hora_nueva", "TIME");
            agregarColumnaSiFalta("reagendamiento_log", "actividad_id", "BIGINT");
            agregarColumnaSiFalta("reagendamiento_log", "mensaje_asistente", "VARCHAR(500) DEFAULT ''");

            agregarColumnaSiFalta("actividades", "peso_prioridad", "INTEGER");
            // Optimistic locking offline sync (@Version). DEFAULT 0 para filas existentes.
            agregarColumnaSiFalta("actividades", "version", "BIGINT NOT NULL DEFAULT 0");
            agregarColumnaSiFalta("horarios_clase", "version", "BIGINT NOT NULL DEFAULT 0");

            relajarColumnaLegacy("reagendamiento_log", "mensaje_asistente");

            jdbcTemplate.execute("UPDATE usuarios SET rol = 'USER' WHERE rol = 'ESTUDIANTE'");
            eliminarColumnaSiExiste("usuarios", "carrera");

            log.info("Parches de esquema de base de datos aplicados correctamente");
        } catch (Exception e) {
            log.warn("No se pudieron aplicar parches de esquema: {}", e.getMessage());
        }
    }

    private boolean esPostgreSQL() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            return meta.getDatabaseProductName().toLowerCase().contains("postgresql");
        } catch (Exception e) {
            return false;
        }
    }

    private void agregarColumnaSiFalta(String tabla, String columna, String definicion) {
        Boolean existe = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns " +
                "WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?)",
                Boolean.class,
                tabla.toLowerCase(),
                columna.toLowerCase()
        );
        if (Boolean.FALSE.equals(existe)) {
            jdbcTemplate.execute("ALTER TABLE " + tabla + " ADD COLUMN " + columna + " " + definicion);
            log.info("Columna añadida: {}.{}", tabla, columna);
        }
    }

    private void relajarColumnaLegacy(String tabla, String columna) {
        Boolean existe = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns " +
                "WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?)",
                Boolean.class,
                tabla.toLowerCase(),
                columna.toLowerCase()
        );
        if (Boolean.TRUE.equals(existe)) {
            try {
                jdbcTemplate.execute("ALTER TABLE " + tabla + " ALTER COLUMN " + columna + " DROP NOT NULL");
                jdbcTemplate.execute("UPDATE " + tabla + " SET " + columna + " = '' WHERE " + columna + " IS NULL");
                jdbcTemplate.execute("ALTER TABLE " + tabla + " ALTER COLUMN " + columna + " SET DEFAULT ''");
                log.info("Columna legacy ajustada: {}.{}", tabla, columna);
            } catch (Exception e) {
                log.warn("No se pudo ajustar {}.{}: {}", tabla, columna, e.getMessage());
            }
        }
    }

    private void eliminarColumnaSiExiste(String tabla, String columna) {
        Boolean existe = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns " +
                "WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?)",
                Boolean.class,
                tabla.toLowerCase(),
                columna.toLowerCase()
        );
        if (Boolean.TRUE.equals(existe)) {
            jdbcTemplate.execute("ALTER TABLE " + tabla + " DROP COLUMN " + columna);
            log.info("Columna eliminada: {}.{}", tabla, columna);
        }
    }
}
