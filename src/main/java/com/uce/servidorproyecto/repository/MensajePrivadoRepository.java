package com.uce.servidorproyecto.repository;

import com.uce.servidorproyecto.model.MensajePrivado;
import com.uce.servidorproyecto.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajePrivadoRepository extends JpaRepository<MensajePrivado, Long> {

    @Query("SELECT m FROM MensajePrivado m WHERE " +
           "(m.remitente = :a AND m.destinatario = :b) OR (m.remitente = :b AND m.destinatario = :a) " +
           "ORDER BY m.fecha ASC")
    List<MensajePrivado> findConversacion(@Param("a") Usuario a, @Param("b") Usuario b);

    @Query("SELECT COUNT(m) FROM MensajePrivado m WHERE m.destinatario = :usuario AND m.leida = false")
    long countNoLeidos(@Param("usuario") Usuario usuario);

    @Query("SELECT COUNT(m) FROM MensajePrivado m WHERE m.destinatario = :usuario AND m.remitente = :otro AND m.leida = false")
    long countNoLeidosDe(@Param("usuario") Usuario usuario, @Param("otro") Usuario otro);

    @Modifying
    @Query("UPDATE MensajePrivado m SET m.leida = true WHERE m.destinatario = :usuario AND m.remitente = :otro AND m.leida = false")
    int marcarLeidosDe(@Param("usuario") Usuario usuario, @Param("otro") Usuario otro);

    @Query("SELECT m FROM MensajePrivado m WHERE m.remitente = :usuario OR m.destinatario = :usuario ORDER BY m.fecha DESC")
    List<MensajePrivado> findRecientesInvolucrando(@Param("usuario") Usuario usuario);

    @Modifying
    @Query("DELETE FROM MensajePrivado m WHERE " +
           "(m.remitente = :a AND m.destinatario = :b) OR (m.remitente = :b AND m.destinatario = :a)")
    int deleteConversacion(@Param("a") Usuario a, @Param("b") Usuario b);
}
