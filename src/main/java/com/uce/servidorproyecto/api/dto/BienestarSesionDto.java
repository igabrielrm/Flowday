package com.uce.servidorproyecto.api.dto;

import com.uce.servidorproyecto.model.RegistroBienestar;

import java.time.LocalDateTime;

public record BienestarSesionDto(
        Long id,
        String tipo,
        Integer valor,
        String descripcion,
        String fecha
) {
    public static BienestarSesionDto from(RegistroBienestar registro) {
        LocalDateTime f = registro.getFecha();
        return new BienestarSesionDto(
                registro.getId(),
                registro.getTipo(),
                registro.getValor(),
                registro.getDescripcion(),
                f != null ? f.toString() : null
        );
    }
}
