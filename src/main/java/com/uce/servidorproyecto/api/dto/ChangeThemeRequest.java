package com.uce.servidorproyecto.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeThemeRequest(@NotBlank String tema) {}
