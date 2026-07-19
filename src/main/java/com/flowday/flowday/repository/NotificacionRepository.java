package com.flowday.flowday.repository;

import com.flowday.flowday.model.Notificacion;
import com.flowday.flowday.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findTop10ByUsuarioOrderByFechaDesc(Usuario usuario);

    long countByUsuarioAndLeidaFalse(Usuario usuario);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.id = :id AND n.usuario = :usuario")
    int marcarLeida(@Param("id") Long id, @Param("usuario") Usuario usuario);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario = :usuario AND n.leida = false")
    int marcarTodasLeidas(@Param("usuario") Usuario usuario);

    @Query("SELECT COUNT(n) > 0 FROM Notificacion n WHERE n.usuario = :usuario AND n.tipo = :tipo AND n.titulo = :titulo AND n.fecha >= :desde")
    boolean existsByUsuarioAndTipoAndTituloDesde(@Param("usuario") Usuario usuario,
                                                  @Param("tipo") String tipo,
                                                  @Param("titulo") String titulo,
                                                  @Param("desde") java.time.LocalDateTime desde);

    @Query("SELECT COUNT(n) > 0 FROM Notificacion n WHERE n.usuario = :usuario AND n.tipo = :tipo AND n.mensaje LIKE :patron AND n.fecha >= :desde")
    boolean existsByUsuarioAndTipoAndMensajeLikeDesde(@Param("usuario") Usuario usuario,
                                                       @Param("tipo") String tipo,
                                                       @Param("patron") String patron,
                                                       @Param("desde") java.time.LocalDateTime desde);
}
