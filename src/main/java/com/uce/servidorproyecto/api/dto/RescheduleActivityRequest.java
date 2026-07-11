package com.uce.servidorproyecto.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleActivityRequest(
        @NotNull LocalDate fecha,
        LocalTime hora
) {}
