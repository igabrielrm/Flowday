package com.flowday.flowday.service;

import com.flowday.flowday.model.Actividad;
import com.flowday.flowday.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstresService {

    @Autowired
    private ActividadService actividadService;

    public Map<String, Object> calcularEstres(Usuario usuario) {
        return calcularEstres(usuario, LocalDate.now());
    }

    public Map<String, Object> calcularEstres(Usuario usuario, LocalDate fecha) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> factores = new ArrayList<>();

        List<Actividad> todas = actividadService.listarPorUsuario(usuario);
        List<Actividad> actividadesDia = actividadService.listarPorFecha(usuario, fecha);

        long pendientesDia = actividadesDia.stream()
                .filter(a -> !"COMPLETADA".equals(a.getEstado()))
                .count();
        long prioridadesAltasDia = actividadesDia.stream()
                .filter(a -> !"COMPLETADA".equals(a.getEstado()))
                .filter(a -> "ALTA".equals(a.getPrioridad()))
                .count();

        long pendientesSemana = todas.stream()
                .filter(a -> !"COMPLETADA".equals(a.getEstado()))
                .filter(a -> a.getFechaInicio() != null)
                .filter(a -> !a.getFechaInicio().isBefore(fecha) && !a.getFechaInicio().isAfter(fecha.plusDays(7)))
                .count();

        long vencenPronto = todas.stream()
                .filter(a -> !"COMPLETADA".equals(a.getEstado()))
                .filter(a -> a.getFechaEntrega() != null)
                .filter(a -> !a.getFechaEntrega().isBefore(fecha) && !a.getFechaEntrega().isAfter(fecha.plusDays(3)))
                .count();

        int minutosDia = actividadesDia.stream()
                .filter(a -> a.getDuracionMinutos() != null)
                .mapToInt(Actividad::getDuracionMinutos)
                .sum();
        int horasEstudio = minutosDia / 60;

        int nivel = 0;

        if (prioridadesAltasDia > 0) {
            int pts = (int) Math.min(45, prioridadesAltasDia * 12);
            nivel += pts;
            factores.add(prioridadesAltasDia + " tarea(s) de ALTA prioridad el " + formatearFecha(fecha) + ".");
        }

        if (pendientesDia > 0) {
            int pts = (int) Math.min(35, pendientesDia * 6);
            nivel += pts;
            factores.add(pendientesDia + " tarea(s) pendiente(s) ese día.");
        }

        if (horasEstudio >= 4) {
            int pts = Math.min(25, horasEstudio * 3);
            nivel += pts;
            factores.add(horasEstudio + " horas programadas.");
        }

        if (pendientesSemana > pendientesDia) {
            int pts = (int) Math.min(20, (pendientesSemana - pendientesDia) * 2);
            nivel += pts;
            factores.add(pendientesSemana + " tareas pendientes en los próximos 7 días.");
        }

        if (vencenPronto > 0) {
            int pts = (int) Math.min(25, vencenPronto * 8);
            nivel += pts;
            factores.add(vencenPronto + " entrega(s) en los próximos 3 días.");
        }

        if (nivel == 0 && pendientesDia == 0) {
            factores.add("Día con poca carga. Aprovecha para descansar.");
        }

        nivel = Math.min(100, nivel);

        String consejo;
        if (nivel >= 70) {
            consejo = "Alto estrés. Prioriza descansos y usa el reagendamiento inteligente.";
        } else if (nivel >= 40) {
            consejo = "Nivel medio. Considera una pausa activa de 5 min cada hora.";
        } else {
            consejo = "Todo tranquilo. Sigue así.";
        }

        resultado.put("nivel", nivel);
        resultado.put("factores", factores);
        resultado.put("consejo", consejo);
        return resultado;
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha.equals(LocalDate.now()) ? "hoy" : fecha.toString();
    }
}
