package com.uce.servidorproyecto.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record SyncOperationResult(
        UUID operationId,
        String kind,
        String status,
        String localEntityId,
        JsonNode data,
        String error,
        Long serverVersion
) {}
