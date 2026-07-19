package com.flowday.flowday.repository;

import com.flowday.flowday.model.ReagendamientoLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReagendamientoLogRepository extends JpaRepository<ReagendamientoLog, Long> {

    List<ReagendamientoLog> findByFechaEjecucionBetweenOrderByFechaEjecucionDesc(
            LocalDateTime desde, LocalDateTime hasta);

    long countByExitosoTrueAndFechaEjecucionBetween(LocalDateTime desde, LocalDateTime hasta);

    long countByExitosoFalseAndFechaEjecucionBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT COUNT(r) FROM ReagendamientoLog r WHERE r.automatico = true " +
           "AND r.fechaEjecucion BETWEEN :desde AND :hasta")
    long countAutomaticosEnPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
