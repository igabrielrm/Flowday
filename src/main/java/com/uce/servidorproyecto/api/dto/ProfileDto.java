package com.uce.servidorproyecto.api.dto;

import com.uce.servidorproyecto.model.Usuario;

public record ProfileDto(
        Long id,
        String nombre,
        String correo,
        String rol,
        String telefono,
        String fechaNacimiento,
        String genero,
        String nombreEmergencia,
        String telefonoEmergencia,
        String relacionEmergencia,
        String tema,
        String foto
) {
    public static ProfileDto from(Usuario u) {
        if (u == null) return null;
        return new ProfileDto(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getRolDisplay(),
                u.getTelefono(),
                u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : null,
                u.getGenero(),
                u.getNombreEmergencia(),
                u.getTelefonoEmergencia(),
                u.getRelacionEmergencia(),
                u.getTema(),
                u.getFoto()
        );
    }
}
