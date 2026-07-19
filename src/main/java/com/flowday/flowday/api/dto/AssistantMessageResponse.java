package com.flowday.flowday.api.dto;

public record AssistantMessageResponse(
        String respuesta,
        AssistantProposalDto proposal,
        boolean ia
) {}
