package com.uce.servidorproyecto.repository;

import com.uce.servidorproyecto.model.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {

    List<Anuncio> findAllByOrderByFechaLimiteDesc();

    List<Anuncio> findByEstadoOrderByFechaPublicacionDesc(String estado);

    @Query("SELECT a FROM Anuncio a WHERE a.fechaLimite >= CURRENT_DATE AND a.estado = 'ACTIVO' ORDER BY a.fechaLimite ASC")
    List<Anuncio> findActivos();

    @Query("SELECT COUNT(a) FROM Anuncio a WHERE a.fechaLimite >= CURRENT_DATE AND a.estado = 'ACTIVO'")
    long countActivos();  // ✅ ESTE MÉTODO ES NECESARIO
}
