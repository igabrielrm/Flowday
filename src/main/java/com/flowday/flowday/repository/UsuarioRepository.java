package com.flowday.flowday.repository;

import com.flowday.flowday.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Usuario> buscarPorNombreOCorreo(@Param("query") String query);

    @Query("SELECT u FROM Usuario u WHERE u.estado = 'ACTIVO' AND u.rol IN ('USER', 'ESTUDIANTE')")
    List<Usuario> findUsuariosActivos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado = 'ACTIVO'")
    long countUsuariosActivos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = 'ADMIN'")
    long countAdmins();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol IN ('USER', 'ESTUDIANTE')")
    long countUsers();
}
