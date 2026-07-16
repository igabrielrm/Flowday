package com.uce.servidorproyecto.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AssistantMessageRequest(
        @NotBlank @Size(max = 1000) String mensaje,
        @Size(max = 20) List<AssistantHistoryMessage> historial,
        @Size(max = 64) String idempotencyKey
) {}
