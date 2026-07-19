package com.flowday.flowday.api.dto;

public record ConversationDto(
        UsuarioDto user,
        String ultimoMensaje,
        String ultimaFecha,
        long noLeidos
) {}
