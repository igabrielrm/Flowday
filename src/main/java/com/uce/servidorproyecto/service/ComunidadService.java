package com.uce.servidorproyecto.service;

import com.uce.servidorproyecto.model.Actividad;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.ActividadRepository;
import com.uce.servidorproyecto.repository.ConexionRepository;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComunidadService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private ConexionRepository conexionRepository;

    // Buscar compañeros
    public List<Usuario> buscarCompaneros(String query, String carrera) {
        List<Usuario> result;
        if (query != null && !query.isEmpty()) {
            result = usuarioRepository.buscarPorNombreOCarrera(query);
        } else if (carrera != null && !carrera.isEmpty()) {
            result = usuarioRepository.buscarPorCarrera(carrera);
        } else {
            result = usuarioRepository.findEstudiantesActivos();
        }
        return result.stream().filter(u -> !"ADMIN".equals(u.getRol())).toList();
    }

    // Calcular compatibilidad (simplificada)
    public Map<Usuario, Integer> calcularCompatibilidad(Usuario usuario) {
        Map<Usuario, Integer> compat = new HashMap<>();
        List<Usuario> otros = usuarioRepository.findEstudiantesActivos().stream()
                .filter(u -> !u.getId().equals(usuario.getId())).toList();

        for (Usuario otro : otros) {
            int puntos = 0;
            if (usuario.getCarrera() != null && usuario.getCarrera().equals(otro.getCarrera())) puntos += 40;
            // Aquí se pueden añadir más factores como materias en común
            compat.put(otro, Math.min(100, puntos));
        }
        return compat;
    }

    // ===== NUEVO: SUGERIR GRUPOS (para el método getSugerencias) =====
    public List<Usuario> sugerirGrupos(Usuario usuario) {
        // Por ahora, devuelve los primeros 4 estudiantes activos que no sean el usuario
        // En el futuro se puede mejorar con algoritmos de afinidad
        return usuarioRepository.findEstudiantesActivos().stream()
                .filter(u -> !u.getId().equals(usuario.getId()))
                .limit(4)
                .collect(Collectors.toList());
    }

    // ===== NUEVO: ESTADÍSTICAS DE COMUNIDAD =====
    public Map<String, Object> getEstadisticasComunidad() {
        Map<String, Object> stats = new HashMap<>();
        long totalUsuarios = usuarioRepository.countUsuariosActivos();
        long totalConexiones = conexionRepository.countAceptadas();
        stats.put("totalUsuarios", totalUsuarios);
        stats.put("totalGrupos", totalConexiones);
        int tasa = totalUsuarios > 0
                ? (int) Math.min(100, Math.round((totalConexiones * 100.0) / totalUsuarios))
                : 0;
        stats.put("tasaConexion", tasa);
        return stats;
    }

    // ===== SINCRONÍA DE HORARIOS (Punto 17) =====
    public List<Map<String, Object>> sugerirHorariosSincronizados(Usuario usuario, Long otroUsuarioId) {
        Usuario otro = usuarioRepository.findById(otroUsuarioId).orElse(null);
        if (otro == null) return List.of();

        List<Actividad> misActividades = actividadRepository.findByUsuarioAndFechaInicio(usuario, LocalDate.now());
        List<Actividad> susActividades = actividadRepository.findByUsuarioAndFechaInicio(otro, LocalDate.now());

        List<Map<String, Object>> sugerencias = new ArrayList<>();
        for (int h = 8; h < 18; h++) {
            for (int m = 0; m < 60; m += 30) {
                LocalTime hora = LocalTime.of(h, m);
                boolean ocupadoYo = misActividades.stream().anyMatch(a -> 
                    a.getHoraInicio() != null && a.getHoraInicio().equals(hora));
                boolean ocupadoEl = susActividades.stream().anyMatch(a -> 
                    a.getHoraInicio() != null && a.getHoraInicio().equals(hora));
                if (!ocupadoYo && !ocupadoEl) {
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("hora", hora.toString());
                    slot.put("disponible", true);
                    sugerencias.add(slot);
                }
            }
        }
        return sugerencias;
    }
}