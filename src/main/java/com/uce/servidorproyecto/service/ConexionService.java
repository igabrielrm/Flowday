package com.uce.servidorproyecto.service;

import com.uce.servidorproyecto.model.Conexion;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.ConexionRepository;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ConexionService {

    public record RelacionInfo(String estadoRelacion, Long conexionId, boolean conectado) {}

    @Autowired
    private ConexionRepository conexionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Transactional
    public Conexion solicitarConexion(Usuario solicitante, Long receptorId) {
        if (receptorId == null) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }
        if (solicitante.getId().equals(receptorId)) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo");
        }

        Usuario receptor = usuarioRepository.findById(receptorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!"ACTIVO".equals(receptor.getEstado()) || "ADMIN".equals(receptor.getRol())) {
            throw new IllegalArgumentException("Ese usuario no está disponible");
        }

        Optional<Conexion> existente = conexionRepository.findBetween(solicitante, receptor);
        if (existente.isPresent()) {
            Conexion c = existente.get();
            if ("ACEPTADA".equals(c.getEstado())) {
                throw new IllegalStateException("Ya están conectados con " + receptor.getNombre());
            }
            if ("PENDIENTE".equals(c.getEstado())) {
                if (c.getReceptor().getId().equals(solicitante.getId())) {
                    throw new IllegalStateException(receptor.getNombre() + " ya te envió una solicitud. Acéptala desde Comunidad.");
                }
                throw new IllegalStateException("Ya enviaste una solicitud a " + receptor.getNombre());
            }
            c.setEstado("PENDIENTE");
            c.setSolicitante(solicitante);
            c.setReceptor(receptor);
            c.setFechaSolicitud(LocalDateTime.now());
            conexionRepository.save(c);
            notificacionService.notificarSolicitudAmistad(receptor, solicitante);
            return c;
        }

        Conexion conexion = new Conexion();
        conexion.setSolicitante(solicitante);
        conexion.setReceptor(receptor);
        conexion.setEstado("PENDIENTE");
        conexionRepository.save(conexion);
        notificacionService.notificarSolicitudAmistad(receptor, solicitante);
        return conexion;
    }

    /** @deprecated usar solicitarConexion */
    @Transactional
    public Conexion conectar(Usuario solicitante, Long receptorId) {
        return solicitarConexion(solicitante, receptorId);
    }

    @Transactional
    public void aceptarSolicitud(Usuario usuario, Long conexionId) {
        Conexion c = requireConexion(conexionId);
        if (!c.getReceptor().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("No puedes aceptar esta solicitud");
        }
        if (!"PENDIENTE".equals(c.getEstado())) {
            throw new IllegalStateException("La solicitud ya no está pendiente");
        }
        c.setEstado("ACEPTADA");
        conexionRepository.save(c);
        notificacionService.notificarConexionAceptada(c.getSolicitante(), usuario);
    }

    @Transactional
    public void rechazarSolicitud(Usuario usuario, Long conexionId) {
        Conexion c = requireConexion(conexionId);
        if (!c.getReceptor().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("No puedes rechazar esta solicitud");
        }
        if (!"PENDIENTE".equals(c.getEstado())) {
            throw new IllegalStateException("La solicitud ya no está pendiente");
        }
        c.setEstado("RECHAZADA");
        conexionRepository.save(c);
    }

    @Transactional
    public void cancelarSolicitud(Usuario usuario, Long conexionId) {
        Conexion c = requireConexion(conexionId);
        if (!c.getSolicitante().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("No puedes cancelar esta solicitud");
        }
        if (!"PENDIENTE".equals(c.getEstado())) {
            throw new IllegalStateException("La solicitud ya no está pendiente");
        }
        conexionRepository.delete(c);
    }

    @Transactional
    public void desconectar(Usuario usuario, Long conexionId) {
        Conexion c = requireConexion(conexionId);
        if (!"ACEPTADA".equals(c.getEstado())) {
            throw new IllegalStateException("No hay una conexión activa");
        }
        boolean participa = c.getSolicitante().getId().equals(usuario.getId())
                || c.getReceptor().getId().equals(usuario.getId());
        if (!participa) {
            throw new IllegalArgumentException("No tienes permiso");
        }
        conexionRepository.delete(c);
    }

    public RelacionInfo obtenerRelacion(Usuario yo, Usuario otro) {
        if (yo == null || otro == null || yo.getId().equals(otro.getId())) {
            return new RelacionInfo("NINGUNA", null, false);
        }
        return conexionRepository.findBetween(yo, otro)
                .map(c -> mapRelacion(c, yo))
                .orElse(new RelacionInfo("NINGUNA", null, false));
    }

    public RelacionInfo obtenerRelacionPorId(Usuario yo, Long conexionId) {
        Conexion c = requireConexion(conexionId);
        boolean participa = c.getSolicitante().getId().equals(yo.getId())
                || c.getReceptor().getId().equals(yo.getId());
        if (!participa) {
            throw new IllegalArgumentException("No tienes permiso sobre esta solicitud");
        }
        return mapRelacion(c, yo);
    }

    private RelacionInfo mapRelacion(Conexion c, Usuario yo) {
        if ("ACEPTADA".equals(c.getEstado())) {
            return new RelacionInfo("CONECTADO", c.getId(), true);
        }
        if ("PENDIENTE".equals(c.getEstado())) {
            if (c.getSolicitante().getId().equals(yo.getId())) {
                return new RelacionInfo("SOLICITUD_ENVIADA", c.getId(), false);
            }
            return new RelacionInfo("SOLICITUD_RECIBIDA", c.getId(), false);
        }
        return new RelacionInfo("NINGUNA", null, false);
    }

    public Set<Long> obtenerIdsConectados(Usuario usuario) {
        Set<Long> ids = new HashSet<>();
        List<Conexion> conexiones = conexionRepository.findAceptadasByUsuario(usuario);
        for (Conexion c : conexiones) {
            if (c.getSolicitante().getId().equals(usuario.getId())) {
                ids.add(c.getReceptor().getId());
            } else {
                ids.add(c.getSolicitante().getId());
            }
        }
        return ids;
    }

    public List<Usuario> obtenerCompanerosConectados(Usuario usuario) {
        List<Usuario> companeros = new ArrayList<>();
        for (Long id : obtenerIdsConectados(usuario)) {
            usuarioRepository.findById(id).ifPresent(companeros::add);
        }
        companeros.sort((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));
        return companeros;
    }

    public long contarConexionesAceptadas() {
        return conexionRepository.countAceptadas();
    }

    public int calcularTasaConexion() {
        long users = usuarioRepository.countUsers();
        if (users == 0) return 0;
        long conexiones = conexionRepository.countAceptadas();
        return (int) Math.min(100, Math.round((conexiones * 100.0) / users));
    }

    private Conexion requireConexion(Long id) {
        return conexionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
    }
}
