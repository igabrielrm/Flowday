package com.uce.servidorproyecto.repository;

import com.uce.servidorproyecto.model.Actividad;
import com.uce.servidorproyecto.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    // ===== LISTAR =====
    List<Actividad> findByUsuario(Usuario usuario);

    List<Actividad> findByUsuarioAndFechaInicio(Usuario usuario, LocalDate fecha);

    List<Actividad> findByUsuarioAndFechaInicioOrderByHoraInicio(Usuario usuario, LocalDate fecha);
/*
    // ===== DETECCIÓN DE CHOQUES - COMPATIBLE H2 Y POSTGRESQL =====
    @Query("SELECT a FROM Actividad a WHERE a.usuario.id = :usuarioId " +
           "AND a.fechaInicio = :fecha " +
           "AND a.id <> :idExcluir " +
           "AND a.horaInicio < :horaFin " +
           "AND FUNCTION('TIMESTAMPADD', 'MINUTE', a.duracionMinutos, a.horaInicio) > :horaInicio")
    List<Actividad> buscarChoques(
        @Param("usuarioId") Long usuarioId,
        @Param("fecha") LocalDate fecha,
        @Param("horaInicio") LocalTime horaInicio,
        @Param("horaFin") LocalTime horaFin,
        @Param("idExcluir") Long idExcluir);
*/
    // ===== CONTADORES =====
    @Query("SELECT COUNT(a) FROM Actividad a WHERE a.usuario = :usuario " +
           "AND a.fechaInicio = :fecha " +
           "AND a.prioridad = 'ALTA' " +
           "AND a.estado <> 'COMPLETADA'")
    long contarPrioritariasPendientes(
        @Param("usuario") Usuario usuario,
        @Param("fecha") LocalDate fecha
    );

    @Query("SELECT COALESCE(SUM(a.duracionMinutos), 0) FROM Actividad a " +
           "WHERE a.usuario = :usuario AND a.fechaInicio = :fecha")
    Integer sumarMinutosDia(
        @Param("usuario") Usuario usuario,
        @Param("fecha") LocalDate fecha
    );
    
    // Para la línea de tiempo
    List<Actividad> findByUsuarioAndFechaInicioAfterOrderByFechaInicioAsc(Usuario usuario, LocalDate fecha);

    // Para alertas (fechas de entrega próximas)
    List<Actividad> findByUsuarioAndFechaEntregaBetween(Usuario usuario, LocalDate inicio, LocalDate fin);

    @Query("SELECT a FROM Actividad a WHERE a.usuario = :usuario " +
           "AND a.estado <> 'COMPLETADA' AND a.estado <> 'CANCELADA' " +
           "AND a.prioridad = 'ALTA'")
    List<Actividad> findAlertasAltaPrioridad(@Param("usuario") Usuario usuario);

    @Query("SELECT a FROM Actividad a WHERE a.usuario = :usuario " +
           "AND a.estado <> 'COMPLETADA' AND a.estado <> 'CANCELADA' " +
           "AND a.fechaEntrega IS NOT NULL AND a.fechaEntrega BETWEEN :inicio AND :fin " +
           "ORDER BY a.fechaEntrega ASC")
    List<Actividad> findAlertasProximasAVencer(
            @Param("usuario") Usuario usuario,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    List<Actividad> findByEstadoNotAndFechaEntregaIsNotNull(String estado);

    @Query("SELECT a FROM Actividad a WHERE a.usuario = :usuario " +
           "AND a.estado <> 'COMPLETADA' " +
           "AND a.tipo IN ('REUNION_GRUPAL', 'CITA_MEDICA', 'CITA_LABORAL', 'TRABAJO_GRUPO') " +
           "ORDER BY a.fechaInicio ASC, a.horaInicio ASC")
    List<Actividad> findReagendables(@Param("usuario") Usuario usuario);

}