package com.flowday.flowday.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ForgotPasswordRequest(
        @NotBlank @Email String correo,
        @NotBlank @Pattern(regexp = "\\d{10}") String telefono
) {}
