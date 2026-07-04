package com.uce.servidorproyecto.service;

import com.uce.servidorproyecto.model.HorarioClase;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HorarioClaseSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(HorarioClaseSchedulerService.class);
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /** Cada minuto: avisa 5 min antes del inicio de clase (una notificación por clase/día). */
    @Scheduled(cron = "0 * * * * *")
    public void avisarInicioClase() {
        LocalTime ahora = LocalTime.now().withSecond(0).withNano(0);
        LocalTime ventanaFin = ahora.plusMinutes(5);

        for (Usuario estudiante : usuarioRepository.findEstudiantesActivos()) {
            List<HorarioClase> clases = horarioService.clasesQueEmpiezanAhora(estudiante, ahora, ventanaFin);
            for (HorarioClase clase : clases) {
                String clave = "horario-" + clase.getId() + "-" + LocalDate.now();
                String titulo = "Clase próxima: " + clase.getMateria();
                String hora = clase.getHoraInicio().format(HORA_FMT);
                String mensaje = "Te toca " + clase.getMateria() + " a las " + hora
                        + (clase.getAula() != null && !clase.getAula().isBlank() ? " · " + clase.getAula() : "");
                notificacionService.crearSiNoExisteHoy(estudiante, "HORARIO_CLASE", titulo, mensaje, "/horario", clave);
            }
        }
    }
}
