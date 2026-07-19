package com.flowday.flowday.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssistantProposalDto(
        UUID id,
        String type,
        String status,
        String summary,
        List<String> conflicts,
        JsonNode payload,
        Instant expiresAt,
        Long activityId
) {}
