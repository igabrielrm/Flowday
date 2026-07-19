package com.flowday.flowday.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeThemeRequest(@NotBlank String tema) {}
