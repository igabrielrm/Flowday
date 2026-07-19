package com.flowday.flowday.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendChatMessageRequest(
        @NotNull Long destinatarioId,
        @NotBlank @Size(max = 2000) String contenido
) {}
