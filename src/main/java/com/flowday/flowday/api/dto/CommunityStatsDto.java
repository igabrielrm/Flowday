package com.flowday.flowday.api.dto;

public record CommunityStatsDto(
        long totalUsuarios,
        long totalConexiones,
        int tasaConexion
) {}
