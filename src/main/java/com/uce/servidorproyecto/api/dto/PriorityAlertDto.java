package com.uce.servidorproyecto.api.dto;

import com.uce.servidorproyecto.model.Actividad;

import java.time.LocalDate;

public record PriorityAlertDto(
        Long id,
        String titulo,
        String tipo,
        String motivo,
        LocalDate fechaEntrega,
        String prioridad
) {
    public static PriorityAlertDto from(Actividad a, String motivo) {
        return new PriorityAlertDto(
                a.getId(),
                a.getTitulo(),
                a.getTipo(),
                motivo,
                a.getFechaEntrega(),
                a.getPrioridad()
        );
    }
}
