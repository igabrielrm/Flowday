package com.uce.servidorproyecto.api.dto;

public record CommunityUserDto(
        UsuarioDto user,
        int compatibilidad,
        boolean conectado,
        String estadoRelacion,
        Long conexionId
) {}
