package com.uce.servidorproyecto.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank String nombre,
        String telefono,
        LocalDate fechaNacimiento,
        String genero,
        String nombreEmergencia,
        String telefonoEmergencia,
        String relacionEmergencia
) {}
