package com.flowday.flowday.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SyncOperationRequest(
        @NotNull UUID operationId,
        @NotBlank String kind,
        Long expectedVersion,
        JsonNode payload,
        String localEntityId
) {}
