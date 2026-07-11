package com.uce.servidorproyecto.api.dto;

public record ChatMessageDto(
        Long id,
        Long remitenteId,
        Long destinatarioId,
        String contenido,
        String fecha,
        boolean leida,
        boolean propio
) {}
