package com.uce.servidorproyecto.api.dto;

public record ConversationDto(
        UsuarioDto user,
        String ultimoMensaje,
        String ultimaFecha,
        long noLeidos
) {}
