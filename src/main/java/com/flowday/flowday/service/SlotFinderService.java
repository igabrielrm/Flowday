package com.flowday.flowday.service;

import com.flowday.flowday.dto.SlotDisponible;
import com.flowday.flowday.model.Actividad;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.model.UsuarioActividad;
import com.flowday.flowday.repository.UsuarioActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SlotFinderService {

    public static final int GRANULARIDAD_MINUTOS = 15;
    public static final int MAX_DIAS_BUSQUEDA = 14;

    @Autowired
    private ConflictDetectionService conflictDetectionService;

    @Autowired
    private UsuarioActividadRepository usuarioActividadRepository;

    public Optional<SlotDisponible> buscarPrimerSlotLibre(Usuario usuario, int duracionMinutos,
                                                            LocalDate desdeFecha, Long idExcluir) {
        if (usuario == null || duracionMinutos <= 0) return Optional.empty();

        LocalDate fecha = desdeFecha != null ? desdeFecha : LocalDate.now();
        if (fecha.isBefore(LocalDate.now())) {
            fecha = LocalDate.now();
        }

        for (int dia = 0; dia < MAX_DIAS_BUSQUEDA; dia++) {
            LocalDate candidata = fecha.plusDays(dia);
            LocalTime cursor = ConflictDetectionService.HORA_MIN;

            while (true) {
                LocalTime fin = cursor.plusMinutes(duracionMinutos);
                if (!fin.isAfter(cursor) || fin.isAfter(ConflictDetectionService.HORA_MAX)) {
                    break;
                }
                if (conflictDetectionService.slotEstaLibre(usuario, candidata, cursor, duracionMinutos, idExcluir)) {
                    return Optional.of(new SlotDisponible(candidata, cursor, fin));
                }
                cursor = cursor.plusMinutes(GRANULARIDAD_MINUTOS);
            }
        }
        return Optional.empty();
    }

    public Optional<SlotDisponible> buscarSlotGrupal(Actividad actividad, int duracionMinutos,
                                                     LocalDate desdeFecha) {
        List<Usuario> miembros = obtenerMiembrosActivos(actividad);
        if (miembros.isEmpty()) {
            return Optional.empty();
        }

        LocalDate fecha = desdeFecha != null ? desdeFecha : actividad.getFechaInicio();
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            fecha = LocalDate.now();
        }

        Long idExcluir = actividad.getId();

        for (int dia = 0; dia < MAX_DIAS_BUSQUEDA; dia++) {
            LocalDate candidata = fecha.plusDays(dia);
            LocalTime cursor = ConflictDetectionService.HORA_MIN;

            while (true) {
                LocalTime fin = cursor.plusMinutes(duracionMinutos);
                if (!fin.isAfter(cursor) || fin.isAfter(ConflictDetectionService.HORA_MAX)) {
                    break;
                }
                boolean todosLibres = true;
                for (Usuario miembro : miembros) {
                    Long excluir = actividad.getUsuario().getId().equals(miembro.getId()) ? idExcluir : null;
                    if (!conflictDetectionService.slotEstaLibre(miembro, candidata, cursor,
                            duracionMinutos, excluir)) {
                        todosLibres = false;
                        break;
                    }
                }
                if (todosLibres) {
                    return Optional.of(new SlotDisponible(candidata, cursor, fin));
                }
                cursor = cursor.plusMinutes(GRANULARIDAD_MINUTOS);
            }
        }
        return Optional.empty();
    }

    public List<Usuario> obtenerMiembrosActivos(Actividad actividad) {
        List<Usuario> miembros = new ArrayList<>();
        if (actividad == null) return miembros;

        miembros.add(actividad.getUsuario());
        for (UsuarioActividad ua : usuarioActividadRepository.findByActividadAndActivoTrue(actividad)) {
            if (!ua.isEsPropietario() && ua.getUsuario() != null) {
                miembros.add(ua.getUsuario());
            }
        }
        return miembros;
    }
}
