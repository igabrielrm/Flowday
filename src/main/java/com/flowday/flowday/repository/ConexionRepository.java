package com.flowday.flowday.repository;

import com.flowday.flowday.model.Conexion;
import com.flowday.flowday.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConexionRepository extends JpaRepository<Conexion, Long> {

    @Query("SELECT c FROM Conexion c WHERE (c.solicitante = :a AND c.receptor = :b) OR (c.solicitante = :b AND c.receptor = :a)")
    Optional<Conexion> findBetween(@Param("a") Usuario a, @Param("b") Usuario b);

    @Query("SELECT c FROM Conexion c WHERE c.estado = 'ACEPTADA' " +
           "AND (c.solicitante = :usuario OR c.receptor = :usuario)")
    List<Conexion> findAceptadasByUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT COUNT(c) > 0 FROM Conexion c WHERE c.estado = 'ACEPTADA' " +
           "AND ((c.solicitante = :a AND c.receptor = :b) OR (c.solicitante = :b AND c.receptor = :a))")
    boolean existenConectados(@Param("a") Usuario a, @Param("b") Usuario b);

    @Query("SELECT COUNT(c) FROM Conexion c WHERE c.estado = 'ACEPTADA'")
    long countAceptadas();
}
