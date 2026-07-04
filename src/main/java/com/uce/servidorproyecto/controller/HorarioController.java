package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.model.HorarioClase;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/horario")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @GetMapping
    public String vistaHorario(WebRequest request, Model model) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        model.addAttribute("usuario", usuario);
        model.addAttribute("horaInicio", HorarioService.HORA_GRID_INICIO);
        model.addAttribute("horaFin", HorarioService.HORA_GRID_FIN);
        return "horario";
    }

    @GetMapping("/api/clases")
    @ResponseBody
    public Map<String, Object> listarClases(WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return Map.of("ok", false, "mensaje", "Sesión expirada");

        List<Map<String, Object>> clases = horarioService.listarPorUsuario(usuario).stream()
                .map(horarioService::toMap)
                .collect(Collectors.toList());
        return Map.of("ok", true, "clases", clases);
    }

    @GetMapping("/api/alerta")
    @ResponseBody
    public Map<String, Object> alertaClase(WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return Map.of("ok", false);

        return horarioService.obtenerAlertaClase(usuario, 15)
                .map(alerta -> {
                    Map<String, Object> res = new HashMap<>();
                    res.put("ok", true);
                    res.put("alerta", alerta);
                    return res;
                })
                .orElse(Map.of("ok", true, "alerta", null));
    }

    @PostMapping("/api/guardar")
    @ResponseBody
    public Map<String, Object> guardar(@RequestParam String materia,
                                       @RequestParam Integer diaSemana,
                                       @RequestParam String horaInicio,
                                       @RequestParam String horaFin,
                                       @RequestParam(required = false) String aula,
                                       @RequestParam(required = false) String profesor,
                                       @RequestParam(required = false) String color,
                                       WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return Map.of("ok", false, "mensaje", "Sesión expirada");

        try {
            HorarioClase datos = buildFromParams(null, materia, diaSemana, horaInicio, horaFin, aula, profesor, color);
            HorarioClase guardada = horarioService.guardar(usuario, datos);
            return Map.of("ok", true, "clase", horarioService.toMap(guardada));
        } catch (Exception e) {
            return Map.of("ok", false, "mensaje", e.getMessage());
        }
    }

    @PostMapping("/api/actualizar/{id}")
    @ResponseBody
    public Map<String, Object> actualizar(@PathVariable Long id,
                                          @RequestParam String materia,
                                          @RequestParam Integer diaSemana,
                                          @RequestParam String horaInicio,
                                          @RequestParam String horaFin,
                                          @RequestParam(required = false) String aula,
                                          @RequestParam(required = false) String profesor,
                                          @RequestParam(required = false) String color,
                                          WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return Map.of("ok", false, "mensaje", "Sesión expirada");

        try {
            HorarioClase datos = buildFromParams(id, materia, diaSemana, horaInicio, horaFin, aula, profesor, color);
            HorarioClase actualizada = horarioService.actualizar(usuario, id, datos);
            return Map.of("ok", true, "clase", horarioService.toMap(actualizada));
        } catch (Exception e) {
            return Map.of("ok", false, "mensaje", e.getMessage());
        }
    }

    @PostMapping("/api/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> eliminar(@PathVariable Long id, WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return Map.of("ok", false, "mensaje", "Sesión expirada");

        try {
            horarioService.eliminar(usuario, id);
            return Map.of("ok", true);
        } catch (Exception e) {
            return Map.of("ok", false, "mensaje", e.getMessage());
        }
    }

    private HorarioClase buildFromParams(Long id, String materia, Integer diaSemana,
                                         String horaInicio, String horaFin,
                                         String aula, String profesor, String color) {
        HorarioClase h = new HorarioClase();
        h.setId(id);
        h.setMateria(materia != null ? materia.trim() : null);
        h.setDiaSemana(diaSemana);
        h.setHoraInicio(LocalTime.parse(horaInicio.length() == 5 ? horaInicio : horaInicio.substring(0, 5)));
        h.setHoraFin(LocalTime.parse(horaFin.length() == 5 ? horaFin : horaFin.substring(0, 5)));
        h.setAula(aula);
        h.setProfesor(profesor);
        h.setColor(color);
        return h;
    }
}
