package com.uce.servidorproyecto.api.dto;

import jakarta.validation.constraints.NotNull;

public record ConnectUserRequest(@NotNull Long userId) {}
