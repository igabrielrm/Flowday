package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    public List<Map<String, Object>> listar(WebRequest request) {
        Usuario usuario = getUsuario(request);
        if (usuario == null) return List.of();
        return notificacionService.listarRecientes(usuario);
    }

    @GetMapping("/contador")
    public Map<String, Long> contador(WebRequest request) {
        Usuario usuario = getUsuario(request);
        if (usuario == null) return Map.of("noLeidas", 0L);
        return Map.of("noLeidas", notificacionService.contarNoLeidas(usuario));
    }

    @PostMapping("/{id}/leer")
    public Map<String, Object> marcarLeida(@PathVariable Long id, WebRequest request) {
        Usuario usuario = getUsuario(request);
        if (usuario == null) return Map.of("ok", false);
        boolean ok = notificacionService.marcarLeida(id, usuario);
        return Map.of("ok", ok, "noLeidas", notificacionService.contarNoLeidas(usuario));
    }

    @PostMapping("/leer-todas")
    public Map<String, Object> marcarTodasLeidas(WebRequest request) {
        Usuario usuario = getUsuario(request);
        if (usuario == null) return Map.of("ok", false);
        notificacionService.marcarTodasLeidas(usuario);
        return Map.of("ok", true, "noLeidas", 0L);
    }

    private Usuario getUsuario(WebRequest request) {
        return (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
    }
}
