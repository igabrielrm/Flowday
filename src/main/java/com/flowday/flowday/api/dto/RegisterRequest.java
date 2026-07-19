package com.flowday.flowday.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String nombre,
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8) String contrasena,
        @NotBlank @Pattern(regexp = "\\d{10}") String telefono,
        String fechaNacimiento,
        String genero
) {}
