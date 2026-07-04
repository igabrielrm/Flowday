package com.uce.servidorproyecto.service;

import com.uce.servidorproyecto.model.Conexion;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.ConexionRepository;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ConexionService {

    @Autowired
    private ConexionRepository conexionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Transactional
    public Conexion conectar(Usuario solicitante, Long receptorId) {
        if (receptorId == null) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }
        if (solicitante.getId().equals(receptorId)) {
            throw new IllegalArgumentException("No puedes conectarte contigo mismo");
        }

        Usuario receptor = usuarioRepository.findById(receptorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!"ESTUDIANTE".equals(receptor.getRol()) || !"ACTIVO".equals(receptor.getEstado())) {
            throw new IllegalArgumentException("Ese usuario no está disponible para conectar");
        }

        if (conexionRepository.existenConectados(solicitante, receptor)) {
            throw new IllegalStateException("Ya estás conectado con " + receptor.getNombre());
        }

        Conexion conexion = new Conexion();
        conexion.setSolicitante(solicitante);
        conexion.setReceptor(receptor);
        conexion.setEstado("ACEPTADA");
        conexionRepository.save(conexion);

        notificacionService.notificarConexion(receptor, solicitante);

        return conexion;
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
        long estudiantes = usuarioRepository.countEstudiantes();
        if (estudiantes == 0) return 0;
        long conexiones = conexionRepository.countAceptadas();
        return (int) Math.min(100, Math.round((conexiones * 100.0) / estudiantes));
    }
}
