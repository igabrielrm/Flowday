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

    @Autowired
    private NotificationPushService notificationPushService;

    public Notificacion crear(Usuario destinatario, String tipo, String titulo, String mensaje, String enlace) {
        Notificacion n = new Notificacion();
        n.setUsuario(destinatario);
        n.setTipo(tipo);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setEnlace(enlace);
        Notificacion saved = notificacionRepository.save(n);
        pushRealtime(destinatario, saved);
        return saved;
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
        Notificacion saved = notificacionRepository.save(n);
        pushRealtime(destinatario, saved);
        return saved;
    }

    private void pushRealtime(Usuario destinatario, Notificacion saved) {
        if (destinatario == null || destinatario.getId() == null) {
            return;
        }
        Map<String, Object> payload = toMap(saved);
        payload.put("noLeidas", contarNoLeidas(destinatario));
        notificationPushService.pushToUser(destinatario.getId(), payload);
    }

    public void notificarConexion(Usuario destinatario, Usuario quienConecto) {
        crear(destinatario, "CONEXION",
                "Nueva conexión",
                quienConecto.getNombre() + " se conectó contigo. ¡Ahora pueden colaborar en actividades!",
                "/app/community");
    }

    public void notificarSolicitudAmistad(Usuario destinatario, Usuario solicitante) {
        crear(destinatario, "SOLICITUD_AMISTAD",
                "Solicitud de amistad",
                solicitante.getNombre() + " quiere conectarse contigo.",
                "/app/community");
    }

    public void notificarConexionAceptada(Usuario destinatario, Usuario quienAcepto) {
        crear(destinatario, "CONEXION",
                "Solicitud aceptada",
                quienAcepto.getNombre() + " aceptó tu solicitud. ¡Ya pueden colaborar!",
                "/app/community");
    }

    @Transactional
    public void notificarAnuncioGlobal(Anuncio anuncio) {
        if (anuncio == null) return;

        String titulo = "Comunicado Flowday: " + anuncio.getTitulo();
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
            mensaje.append("Nuevo comunicado publicado.");
        }

        for (Usuario usuario : usuarioRepository.findUsuariosActivos()) {
            crear(usuario, "ANUNCIO", titulo, mensaje.toString(), "anuncio:" + anuncio.getId());
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

    @Transactional
    public boolean eliminar(Long id, Usuario usuario) {
        return notificacionRepository.findById(id)
                .filter(n -> n.getUsuario().getId().equals(usuario.getId()))
                .map(n -> {
                    notificacionRepository.delete(n);
                    return true;
                })
                .orElse(false);
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
