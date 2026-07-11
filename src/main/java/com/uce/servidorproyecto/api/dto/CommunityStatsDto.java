package com.uce.servidorproyecto.api.dto;

public record CommunityStatsDto(
        long totalUsuarios,
        long totalConexiones,
        int tasaConexion
) {}
