package com.uce.servidorproyecto.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(min = 8) String contrasenaNueva,
        @NotBlank @Size(min = 8) String contrasenaConfirmacion
) {}
