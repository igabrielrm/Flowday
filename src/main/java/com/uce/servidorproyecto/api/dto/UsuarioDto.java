package com.uce.servidorproyecto.api.dto;

import com.uce.servidorproyecto.model.Usuario;

public record UsuarioDto(Long id, String nombre, String correo, String rol, String tema, String foto) {

    public static UsuarioDto from(Usuario u) {
        if (u == null) return null;
        return new UsuarioDto(u.getId(), u.getNombre(), u.getCorreo(), u.getRolDisplay(), u.getTema(), u.getFoto());
    }
}
