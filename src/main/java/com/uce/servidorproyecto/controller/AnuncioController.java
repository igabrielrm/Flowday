package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.model.Anuncio;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.AnuncioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/anuncios")
public class AnuncioController {

    @Autowired
    private AnuncioRepository anuncioRepository;

    @GetMapping("/{id}")
    public Map<String, Object> detalle(@PathVariable Long id, WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) {
            return Map.of("ok", false, "mensaje", "Sesión expirada");
        }

        Anuncio anuncio = anuncioRepository.findById(id).orElse(null);
        if (anuncio == null) {
            return Map.of("ok", false, "mensaje", "Comunicado no encontrado");
        }

        if (!"ADMIN".equals(usuario.getRol()) && !"ACTIVO".equals(anuncio.getEstado())) {
            return Map.of("ok", false, "mensaje", "Comunicado no disponible");
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("ok", true);
        resp.put("id", anuncio.getId());
        resp.put("titulo", anuncio.getTitulo());
        resp.put("descripcion", anuncio.getDescripcion());
        resp.put("fechaLimite", anuncio.getFechaLimite() != null ? anuncio.getFechaLimite().toString() : null);
        resp.put("fechaPublicacion", anuncio.getFechaPublicacion() != null
                ? anuncio.getFechaPublicacion().toLocalDate().toString() : null);
        resp.put("estado", anuncio.getEstado());
        return resp;
    }
}
