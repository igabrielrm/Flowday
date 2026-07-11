package com.uce.servidorproyecto.api.dto;

public record ScheduleBlockDto(
        Long id,
        String materia,
        Integer diaSemana,
        String diaNombre,
        String horaInicio,
        String horaFin,
        String aula,
        String profesor,
        String color
) {}
