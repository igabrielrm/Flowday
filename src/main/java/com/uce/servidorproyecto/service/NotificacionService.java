package com.uce.servidorproyecto.service;

import com.uce.servidorproyecto.model.Anuncio;
import com.uce.servidorproyecto.model.Notificacion;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.NotificacionRepository;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Notificacion crear(Usuario destinatario, String tipo, String titulo, String mensaje, String enlace) {
        Notificacion n = new Notificacion();
        n.setUsuario(destinatario);
        n.setTipo(tipo);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setEnlace(enlace);
        return notificacionRepository.save(n);
    }

    public Notificacion crearSiNoExisteHoy(Usuario destinatario, String tipo, String titulo,
                                           String mensaje, String enlace, String claveDedupe) {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        if (claveDedupe != null && notificacionRepository.existsByUsuarioAndTipoAndMensajeLikeDesde(
                destinatario, tipo, "%[" + claveDedupe + "]%", inicioDia)) {
            return null;
        }
        Notificacion n = new Notificacion();
        n.setUsuario(destinatario);
        n.setTipo(tipo);
        n.setTitulo(titulo);
        n.setMensaje(claveDedupe != null ? "[" + claveDedupe + "] " + mensaje : mensaje);
        n.setEnlace(enlace);
        return notificacionRepository.save(n);
    }

    public void notificarConexion(Usuario destinatario, Usuario quienConecto) {
        crear(destinatario, "CONEXION",
                "Nueva conexión",
                quienConecto.getNombre() + " se conectó contigo. ¡Ahora pueden colaborar en actividades!",
                "/comunidad");
    }

    @Transactional
    public void notificarAnuncioGlobal(Anuncio anuncio) {
        if (anuncio == null) return;

        String titulo = "Comunicado UCE: " + anuncio.getTitulo();
        StringBuilder mensaje = new StringBuilder();
        if (anuncio.getDescripcion() != null && !anuncio.getDescripcion().isBlank()) {
            String desc = anuncio.getDescripcion().trim();
            mensaje.append(desc.length() > 180 ? desc.substring(0, 177) + "..." : desc);
        }
        if (anuncio.getFechaLimite() != null) {
            if (mensaje.length() > 0) mensaje.append(" — ");
            mensaje.append("Fecha límite: ").append(anuncio.getFechaLimite());
        }
        if (mensaje.length() == 0) {
            mensaje.append("Nuevo comunicado institucional publicado.");
        }

        for (Usuario estudiante : usuarioRepository.findEstudiantesActivos()) {
            crear(estudiante, "ANUNCIO", titulo, mensaje.toString(), "anuncio:" + anuncio.getId());
        }
    }

    public long contarNoLeidas(Usuario usuario) {
        return notificacionRepository.countByUsuarioAndLeidaFalse(usuario);
    }

    public List<Map<String, Object>> listarRecientes(Usuario usuario) {
        return notificacionRepository.findTop10ByUsuarioOrderByFechaDesc(usuario)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean marcarLeida(Long id, Usuario usuario) {
        return notificacionRepository.marcarLeida(id, usuario) > 0;
    }

    @Transactional
    public int marcarTodasLeidas(Usuario usuario) {
        return notificacionRepository.marcarTodasLeidas(usuario);
    }

    private Map<String, Object> toMap(Notificacion n) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", n.getId());
        m.put("tipo", n.getTipo());
        m.put("titulo", n.getTitulo());
        m.put("mensaje", limpiarMensajeNotif(n.getMensaje()));
        m.put("enlace", n.getEnlace());
        m.put("leida", n.isLeida());
        m.put("fecha", n.getFecha() != null ? n.getFecha().toString() : null);
        return m;
    }

    private String limpiarMensajeNotif(String mensaje) {
        if (mensaje != null && mensaje.startsWith("[") && mensaje.contains("] ")) {
            return mensaje.substring(mensaje.indexOf("] ") + 2);
        }
        return mensaje;
    }
}
