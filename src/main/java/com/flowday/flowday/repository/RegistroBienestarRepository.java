package com.flowday.flowday.repository;

import com.flowday.flowday.model.RegistroBienestar;
import com.flowday.flowday.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroBienestarRepository extends JpaRepository<RegistroBienestar, Long> {

    List<RegistroBienestar> findByUsuarioOrderByFechaDesc(Usuario usuario);

    List<RegistroBienestar> findByUsuarioAndTipoOrderByFechaDesc(Usuario usuario, String tipo);

    // ✅ ESTE MÉTODO ES NECESARIO
    @Query("SELECT COUNT(r) FROM RegistroBienestar r WHERE r.usuario = :usuario AND r.tipo = :tipo AND r.fecha >= :fecha")
    long countByUsuarioAndTipoAndFechaAfter(
        @Param("usuario") Usuario usuario,
        @Param("tipo") String tipo,
        @Param("fecha") LocalDateTime fecha
    );

    // ✅ ESTE MÉTODO TAMBIÉN ES NECESARIO
    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM RegistroBienestar r " +
           "WHERE r.usuario = :usuario AND r.tipo = 'POMODORO' AND r.fecha >= :fechaInicio")
    Integer sumarMinutosPomodoro(
        @Param("usuario") Usuario usuario,
        @Param("fechaInicio") LocalDateTime fechaInicio
    );

    @Query("SELECT COUNT(r) FROM RegistroBienestar r WHERE r.tipo = :tipo AND r.fecha >= :fecha")
    long countAllByTipoAndFechaAfter(@Param("tipo") String tipo, @Param("fecha") LocalDateTime fecha);

    @Query("SELECT COUNT(r) FROM RegistroBienestar r WHERE r.tipo LIKE 'PAUSA_%' AND r.fecha >= :fecha")
    long countAllPausasAfter(@Param("fecha") LocalDateTime fecha);

    @Query("SELECT COUNT(r) FROM RegistroBienestar r WHERE r.usuario = :usuario AND r.tipo LIKE 'PAUSA_%' AND r.fecha >= :fecha")
    long countPausasByUsuarioSince(
            @Param("usuario") Usuario usuario,
            @Param("fecha") LocalDateTime fecha);
}
