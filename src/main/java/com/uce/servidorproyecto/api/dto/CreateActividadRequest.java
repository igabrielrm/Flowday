package com.uce.servidorproyecto.api.dto;



import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;



import java.time.LocalDate;

import java.time.LocalTime;

import java.util.List;



public record CreateActividadRequest(

        @NotBlank String titulo,

        @NotBlank String tipo,

        @NotNull LocalDate fechaInicio,

        LocalTime horaInicio,

        Integer duracionMinutos,

        String materia,

        String prioridad,

        LocalDate fechaEntrega,

        String descripcion,

        List<Long> companerosIds,
        String color
) {}

