package com.flowday.flowday.service;

import com.flowday.flowday.model.Notificacion;
import com.flowday.flowday.model.ReagendamientoLog;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.repository.NotificacionRepository;
import com.flowday.flowday.repository.ReagendamientoLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operaciones en transacción independiente para no abortar el flujo principal en PostgreSQL.
 */
@Service
public class TransaccionAisladaService {

    @Autowired
    private ReagendamientoLogRepository reagendamientoLogRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guardarReagendamientoLog(ReagendamientoLog log) {
        if (log.getMensajeAsistente() == null || log.getMensajeAsistente().isBlank()) {
            log.setMensajeAsistente(log.getMotivo() != null ? log.getMotivo() : "Reagendamiento automático");
        }
        reagendamientoLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notificacion crearNotificacion(Usuario destinatario, String tipo, String titulo,
                                          String mensaje, String enlace) {
        Notificacion n = new Notificacion();
        n.setUsuario(destinatario);
        n.setTipo(tipo);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setEnlace(enlace);
        return notificacionRepository.save(n);
    }
}
