package com.flowday.flowday.service;

import com.flowday.flowday.model.Actividad;
import com.flowday.flowday.repository.ActividadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrioridadSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(PrioridadSchedulerService.class);

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private NotificacionService notificacionService;

    /** Ejecuta todos los días a las 07:00 (America/Guayaquil si el servidor está en esa zona). */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void actualizarPrioridadesDiarias() {
        log.info("Iniciando job diario de prioridades...");
        List<Actividad> pendientes = actividadRepository.findByEstadoNotAndFechaEntregaIsNotNull("COMPLETADA");
        int actualizadas = 0;

        for (Actividad actividad : pendientes) {
            String prioridadAnterior = actividad.getPrioridad();
            String nuevaPrioridad = actividadService.calcularPrioridadAutomatica(actividad);

            if (nuevaPrioridad != null && !nuevaPrioridad.equals(prioridadAnterior)) {
                actividad.setPrioridad(nuevaPrioridad);
                actividadRepository.save(actividad);
                actualizadas++;

                if ("ALTA".equals(nuevaPrioridad) && actividad.getUsuario() != null) {
                    long dias = java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDate.now(), actividad.getFechaEntrega());
                    String mensaje = dias <= 0
                            ? "La tarea \"" + actividad.getTitulo() + "\" vence hoy."
                            : "La tarea \"" + actividad.getTitulo() + "\" vence en " + dias + " día(s).";
                    notificacionService.crear(actividad.getUsuario(), "PRIORIDAD",
                            "⚠️ Prioridad elevada a ALTA",
                            mensaje,
                            "/actividades");
                }
            }
        }
        log.info("Job de prioridades finalizado. {} actividades actualizadas.", actualizadas);
    }
}
