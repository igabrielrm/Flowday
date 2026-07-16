package com.uce.servidorproyecto.api.dto;

public record AssistantMessageResponse(
        String respuesta,
        AssistantProposalDto proposal,
        boolean ia
) {}
