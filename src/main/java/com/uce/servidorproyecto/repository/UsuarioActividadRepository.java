package com.uce.servidorproyecto.repository;

import com.uce.servidorproyecto.model.Actividad;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.model.UsuarioActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioActividadRepository extends JpaRepository<UsuarioActividad, Long> {

    @Query("SELECT ua.actividad FROM UsuarioActividad ua " +
           "WHERE ua.usuario = :usuario AND ua.activo = true AND ua.esPropietario = false")
    List<Actividad> findActividadesCompartidasConUsuario(@Param("usuario") Usuario usuario);

    List<UsuarioActividad> findByActividadAndActivoTrue(Actividad actividad);

    Optional<UsuarioActividad> findByActividadAndUsuario(Actividad actividad, Usuario usuario);

    @Query("SELECT ua.usuario FROM UsuarioActividad ua " +
           "WHERE ua.actividad = :actividad AND ua.activo = true AND ua.esPropietario = false")
    List<Usuario> findCompanerosInvitados(@Param("actividad") Actividad actividad);

    List<UsuarioActividad> findByActividad(Actividad actividad);

    void deleteByActividad(Actividad actividad);
}
