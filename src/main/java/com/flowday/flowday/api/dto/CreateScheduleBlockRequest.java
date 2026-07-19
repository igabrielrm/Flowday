package com.flowday.flowday.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CreateScheduleBlockRequest(
        @NotBlank String materia,
        @NotNull @Min(1) @Max(7) Integer diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        String aula,
        String profesor,
        String color
) {}
