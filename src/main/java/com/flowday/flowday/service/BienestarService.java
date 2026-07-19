package com.flowday.flowday.service;

import com.flowday.flowday.api.dto.BienestarSesionDto;
import com.flowday.flowday.model.RegistroBienestar;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.repository.RegistroBienestarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BienestarService {

    @Autowired
    private RegistroBienestarRepository registroBienestarRepository;

    // ===== GUARDAR SESIÓN =====
    public RegistroBienestar guardarSesion(Usuario usuario, String tipo, Integer valor, String descripcion) {
        RegistroBienestar registro = new RegistroBienestar();
        registro.setUsuario(usuario);
        registro.setTipo(tipo);
        registro.setValor(valor);
        registro.setDescripcion(descripcion);
        registro.setFecha(LocalDateTime.now());
        return registroBienestarRepository.save(registro);
    }

    // ===== GUARDAR SESIÓN POMODORO =====
    public RegistroBienestar guardarPomodoro(Usuario usuario, Integer duracion) {
        return guardarSesion(usuario, "POMODORO", duracion, "Sesión Pomodoro completada");
    }

    // ===== GUARDAR PAUSA ACTIVA =====
    public RegistroBienestar guardarPausaActiva(Usuario usuario, String tipoPausa, Integer duracion) {
        return guardarSesion(usuario, "PAUSA_" + tipoPausa.toUpperCase(), duracion, "Pausa activa: " + tipoPausa);
    }

    // ===== OBTENER ESTADÍSTICAS =====
    public Map<String, Object> getEstadisticasBienestar(Usuario usuario) {
        Map<String, Object> stats = new HashMap<>();

        // Sesiones Pomodoro de la semana
        LocalDateTime inicioSemana = LocalDateTime.now().minusDays(7);
        Integer minutosPomodoro = registroBienestarRepository.sumarMinutosPomodoro(usuario, inicioSemana);

        stats.put("minutosPomodoro", minutosPomodoro != null ? minutosPomodoro : 0);
        stats.put("sesionesPomodoro", minutosPomodoro != null ? minutosPomodoro / 25 : 0);

        // Obtener últimas sesiones
        List<RegistroBienestar> ultimas = registroBienestarRepository
                .findByUsuarioOrderByFechaDesc(usuario);
        stats.put("ultimasSesiones", ultimas.stream()
                .limit(10)
                .map(BienestarSesionDto::from)
                .toList());

        stats.put("totalPomodoros", registroBienestarRepository.countByUsuarioAndTipoAndFechaAfter(
                usuario, "POMODORO", inicioSemana));
        stats.put("totalPausas", registroBienestarRepository.countPausasByUsuarioSince(usuario, inicioSemana));

        return stats;
    }

   // ===== CONTAR SESIONES POR TIPO =====
    public long contarSesionesPorTipo(Usuario usuario, String tipo, LocalDateTime desde) {
        return registroBienestarRepository.countByUsuarioAndTipoAndFechaAfter(usuario, tipo, desde);
    }
}

