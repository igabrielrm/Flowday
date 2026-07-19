package com.flowday.flowday.api.dto;

import com.flowday.flowday.model.Actividad;

import java.time.LocalDate;

public record PriorityAlertDto(
        Long id,
        Long version,
        String titulo,
        String tipo,
        String motivo,
        LocalDate fechaEntrega,
        String prioridad
) {
    public static PriorityAlertDto from(Actividad a, String motivo) {
        return new PriorityAlertDto(
                a.getId(),
                a.getVersion(),
                a.getTitulo(),
                a.getTipo(),
                motivo,
                a.getFechaEntrega(),
                a.getPrioridad()
        );
    }
}
