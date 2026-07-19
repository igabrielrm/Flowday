package com.flowday.flowday.service;

import com.flowday.flowday.model.BloqueRecurrente;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.repository.UsuarioRepository;
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
public class BloqueRecurrenteSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(BloqueRecurrenteSchedulerService.class);
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Scheduled(cron = "0 * * * * *")
    public void avisarInicioBloque() {
        LocalTime ahora = LocalTime.now().withSecond(0).withNano(0);
        LocalTime ventanaFin = ahora.plusMinutes(5);

        for (Usuario usuario : usuarioRepository.findUsuariosActivos()) {
            List<BloqueRecurrente> bloques = horarioService.bloquesQueEmpiezanAhora(usuario, ahora, ventanaFin);
            for (BloqueRecurrente bloque : bloques) {
                String clave = "horario-" + bloque.getId() + "-" + LocalDate.now();
                String titulo = "Bloque próximo: " + bloque.getMateria();
                String hora = bloque.getHoraInicio().format(HORA_FMT);
                String mensaje = "Te toca " + bloque.getMateria() + " a las " + hora
                        + (bloque.getAula() != null && !bloque.getAula().isBlank() ? " · " + bloque.getAula() : "");
                notificacionService.crearSiNoExisteHoy(usuario, "HORARIO_BLOQUE", titulo, mensaje, "/horario", clave);
            }
        }
    }
}
