package com.flowday.flowday.api.dto;

public record ScheduleBlockDto(
        Long id,
        Long version,
        String materia,
        Integer diaSemana,
        String diaNombre,
        String horaInicio,
        String horaFin,
        String aula,
        String profesor,
        String color
) {}
