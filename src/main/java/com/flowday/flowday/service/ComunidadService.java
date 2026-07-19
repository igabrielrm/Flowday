package com.flowday.flowday.service;

import com.flowday.flowday.model.Actividad;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.repository.ActividadRepository;
import com.flowday.flowday.repository.ConexionRepository;
import com.flowday.flowday.repository.UsuarioRepository;
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

    public List<Usuario> buscarCompaneros(String query) {
        List<Usuario> result;
        if (query != null && !query.isEmpty()) {
            result = usuarioRepository.buscarPorNombreOCorreo(query);
        } else {
            result = usuarioRepository.findUsuariosActivos();
        }
        return result.stream().filter(u -> !"ADMIN".equals(u.getRol())).toList();
    }

    public Map<Usuario, Integer> calcularCompatibilidad(Usuario usuario) {
        Map<Usuario, Integer> compat = new HashMap<>();
        List<Usuario> otros = usuarioRepository.findUsuariosActivos().stream()
                .filter(u -> !u.getId().equals(usuario.getId())).toList();

        Set<String> misMaterias = actividadRepository.findByUsuario(usuario).stream()
                .map(Actividad::getMateria)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Usuario otro : otros) {
            int puntos = 0;
            long materiasComunes = actividadRepository.findByUsuario(otro).stream()
                    .map(Actividad::getMateria)
                    .filter(m -> m != null && misMaterias.contains(m))
                    .distinct()
                    .count();
            puntos += (int) Math.min(40, materiasComunes * 10);
            compat.put(otro, Math.min(100, puntos));
        }
        return compat;
    }

    public List<Usuario> sugerirGrupos(Usuario usuario) {
        return usuarioRepository.findUsuariosActivos().stream()
                .filter(u -> !u.getId().equals(usuario.getId()))
                .limit(4)
                .collect(Collectors.toList());
    }

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
