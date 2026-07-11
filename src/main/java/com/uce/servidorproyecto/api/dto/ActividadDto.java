package com.uce.servidorproyecto.api.dto;

import com.uce.servidorproyecto.model.Actividad;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.service.ActividadService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ActividadDto(
        Long id,
        String titulo,
        String descripcion,
        String tipo,
        String estado,
        LocalDate fechaInicio,
        LocalTime horaInicio,
        Integer duracionMinutos,
        String materia,
        String prioridad,
        LocalDate fechaEntrega,
        String color,
        boolean esPropietario,
        boolean puedeEditar,
        List<Long> companerosIds
) {
    public static ActividadDto from(Actividad a, Usuario viewer, ActividadService service, List<Long> companerosIds) {
        if (a == null) return null;
        boolean puedeEditar = viewer != null && service != null && service.puedeEditar(viewer, a);
        boolean esPropietario = viewer != null && service != null
                && Boolean.TRUE.equals(service.toListaMap(a, viewer).get("esPropietario"));
        String estado = a.getEstado();
        if (viewer != null && service != null) {
            estado = String.valueOf(service.toListaMap(a, viewer).get("estado"));
        }
        return new ActividadDto(
                a.getId(),
                a.getTitulo(),
                a.getDescripcion(),
                a.getTipo(),
                estado,
                a.getFechaInicio(),
                a.getHoraInicio(),
                a.getDuracionMinutos(),
                a.getMateria(),
                a.getPrioridad(),
                a.getFechaEntrega(),
                a.getColor(),
                esPropietario,
                puedeEditar,
                companerosIds != null ? companerosIds : List.of()
        );
    }
}
