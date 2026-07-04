package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import com.uce.servidorproyecto.service.ComunidadService;
import com.uce.servidorproyecto.service.ConexionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/comunidad")
public class ComunidadController {

    @Autowired
    private ComunidadService comunidadService;

    @Autowired
    private ConexionService conexionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ===== PÁGINA DE COMUNIDAD =====
    @GetMapping
    public String comunidad(WebRequest request, Model model) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        // Obtener todos los usuarios excepto el actual
        List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> !u.getId().equals(usuario.getId()))
                .filter(u -> !"ADMIN".equals(u.getRol()))
                .toList();

        // Calcular compatibilidad
        Map<Usuario, Integer> compatibilidad = comunidadService.calcularCompatibilidad(usuario);
        Set<Long> conectados = conexionService.obtenerIdsConectados(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("compatibilidad", compatibilidad);
        model.addAttribute("conectados", conectados);
        model.addAttribute("sugerencias", comunidadService.sugerirGrupos(usuario));
        model.addAttribute("estadisticas", comunidadService.getEstadisticasComunidad());

        return "community";
    }

    // ===== API: BUSCAR USUARIOS =====
    @GetMapping("/api/buscar")
    @ResponseBody
    public List<Usuario> buscarUsuarios(@RequestParam(required = false) String query,
                                        @RequestParam(required = false) String carrera) {
        return comunidadService.buscarCompaneros(query, carrera);
    }

    // ===== API: COMPATIBILIDAD =====
    @GetMapping("/api/compatibilidad")
    @ResponseBody
    public Map<Usuario, Integer> getCompatibilidad(WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return Map.of();
        return comunidadService.calcularCompatibilidad(usuario);
    }

    // ===== API: SUGERENCIAS =====
    @GetMapping("/api/sugerencias")
    @ResponseBody
    public List<Usuario> getSugerencias(WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return List.of();
        return comunidadService.sugerirGrupos(usuario);
    }
    
    @GetMapping("/api/sincronia/{id}")
    @ResponseBody
    public List<Map<String, Object>> getSincronia(@PathVariable Long id, WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return List.of();
        return comunidadService.sugerirHorariosSincronizados(usuario, id);
    }

    // ===== API: CONECTAR CON COMPAÑERO (automático) =====
    @PostMapping("/api/conectar/{id}")
    @ResponseBody
    public Map<String, Object> conectar(@PathVariable Long id, WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) {
            return Map.of("ok", false, "mensaje", "Sesión expirada");
        }
        try {
            conexionService.conectar(usuario, id);
            return Map.of("ok", true, "mensaje", "Conexión establecida correctamente");
        } catch (IllegalStateException e) {
            return Map.of("ok", false, "mensaje", e.getMessage());
        } catch (IllegalArgumentException e) {
            return Map.of("ok", false, "mensaje", e.getMessage());
        }
    }
}
