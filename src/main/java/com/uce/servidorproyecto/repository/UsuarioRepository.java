package com.uce.servidorproyecto.repository;

import com.uce.servidorproyecto.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // ===== BÚSQUEDA =====
    Optional<Usuario> findByCorreo(String correo);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.carrera) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Usuario> buscarPorNombreOCarrera(@Param("query") String query);

    @Query("SELECT u FROM Usuario u WHERE u.carrera = :carrera")
    List<Usuario> buscarPorCarrera(@Param("carrera") String carrera);

    @Query("SELECT u FROM Usuario u WHERE u.estado = 'ACTIVO' AND u.rol = 'ESTUDIANTE'")
    List<Usuario> findEstudiantesActivos();

    // ===== CONTADORES =====
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado = 'ACTIVO'")
    long countUsuariosActivos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = 'ADMIN'")
    long countAdmins();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = 'ESTUDIANTE'")
    long countEstudiantes();
}