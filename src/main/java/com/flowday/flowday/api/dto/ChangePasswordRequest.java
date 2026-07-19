package com.flowday.flowday.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String contrasenaActual,
        @NotBlank String contrasenaNueva,
        @NotBlank String contrasenaConfirmacion
) {}
