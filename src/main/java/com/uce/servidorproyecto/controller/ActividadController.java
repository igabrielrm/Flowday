package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.model.Actividad;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.service.ActividadCompartidaService;
import com.uce.servidorproyecto.service.ActividadService;
import com.uce.servidorproyecto.service.ConexionService;
import com.uce.servidorproyecto.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/actividades")
public class ActividadController {

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private ConexionService conexionService;

    @Autowired
    private ActividadCompartidaService actividadCompartidaService;

    @Autowired
    private HorarioService horarioService;

    private void cargarDatosFormulario(Usuario usuario, Model model, Actividad actividad) {
        model.addAttribute("companerosConectados", conexionService.obtenerCompanerosConectados(usuario));
        model.addAttribute("materiasHorario", horarioService.listarMateriasDistintas(usuario));
        if (actividad.getId() != null) {
            model.addAttribute("companerosVinculados",
                    actividadCompartidaService.obtenerIdsCompanerosVinculados(actividad));
        }
    }

    // ===== LISTAR TODAS LAS ACTIVIDADES DEL USUARIO =====
    @GetMapping
    public String listar(WebRequest request, Model model) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        List<Actividad> actividades = actividadService.listarPorUsuario(usuario);
        List<Map<String, Object>> actividadesInfo = actividades.stream()
                .map(a -> actividadService.toListaMap(a, usuario))
                .collect(Collectors.toList());
        model.addAttribute("actividades", actividades);
        model.addAttribute("actividadesInfo", actividadesInfo);
        model.addAttribute("usuario", usuario);
        return "lista-actividades";
    }

    // ===== FORMULARIO NUEVA ACTIVIDAD =====
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                        WebRequest request, Model model) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        Actividad actividad = new Actividad();
        if (fecha != null) {
            actividad.setFechaInicio(fecha);
            if (fecha.isBefore(LocalDate.now())) {
                model.addAttribute("errorChoque", "⚠️ No puedes crear actividades en fechas pasadas.");
            }
        }

        model.addAttribute("actividad", actividad);
        model.addAttribute("usuario", usuario);
        model.addAttribute("modo", "nueva");
        cargarDatosFormulario(usuario, model, actividad);
        return "formulario-actividad";
    }

    // ===== GUARDAR NUEVA ACTIVIDAD =====
    @PostMapping
    public String guardar(@ModelAttribute("actividad") Actividad actividad,
                          @RequestParam(value = "companerosIds", required = false) List<Long> companerosIds,
                          WebRequest request,
                          RedirectAttributes ra) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        List<String> errores = actividadService.validarActividad(actividad);
        if (!errores.isEmpty()) {
            ra.addFlashAttribute("errorChoque", "⚠️ " + String.join(". ", errores));
            return "redirect:/actividades/nueva";
        }

        // Verificar choques de horario
        if (actividad.getFechaInicio() != null && actividad.getHoraInicio() != null) {
            boolean hayChoque = actividadService.hayChoque(
                    usuario,
                    actividad.getFechaInicio(),
                    actividad.getHoraInicio(),
                    actividad.getDuracionMinutos(),
                    null
            );
            if (hayChoque) {
                ra.addFlashAttribute("errorChoque",
                        "⚠️ Ya tienes una actividad en ese horario. Elige otra hora.");
                return "redirect:/actividades/nueva";
            }
        }

        // ===== NUEVO: PRIORIDAD AUTOMÁTICA =====
        if (actividad.getPrioridad() == null || actividad.getPrioridad().isEmpty()) {
            actividad.setPrioridad(actividadService.calcularPrioridadAutomatica(actividad));
        }

        // Determinar dinámicamente si la actividad es académica o personal
        String tipo = actividad.getTipo();
        if ("CITA_MEDICA".equals(tipo) || "CITA_LABORAL".equals(tipo) || "OTRO".equals(tipo)) {
            actividad.setEsAcademico(false);
        } else {
            actividad.setEsAcademico(true);
        }

        actividad.setUsuario(usuario);
        actividadService.guardar(actividad);
        actividadCompartidaService.registrarPropietario(actividad, usuario);

        if (esTipoGrupal(actividad.getTipo())) {
            actividadCompartidaService.vincularCompaneros(actividad, usuario, companerosIds);
        }

        ra.addFlashAttribute("exito", "✅ Actividad guardada correctamente");
        return "redirect:/dashboard";
    }

    private boolean esTipoGrupal(String tipo) {
        return "REUNION_GRUPAL".equals(tipo) || "TRABAJO_GRUPO".equals(tipo);
    }

    // ===== FORMULARIO EDITAR ACTIVIDAD =====
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, WebRequest request, Model model, RedirectAttributes ra) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        Actividad actividad = actividadService.buscarPorId(id);
        if (actividad == null || !actividadService.puedeAcceder(usuario, actividad)) {
            return "redirect:/actividades";
        }

        if (!actividadService.puedeEditar(usuario, actividad)) {
            model.addAttribute("actividad", actividad);
            model.addAttribute("usuario", usuario);
            model.addAttribute("modo", "ver");
            model.addAttribute("soloLectura", true);
            cargarDatosFormulario(usuario, model, actividad);
            return "formulario-actividad";
        }

        model.addAttribute("actividad", actividad);
        model.addAttribute("usuario", usuario);
        model.addAttribute("modo", "editar");
        model.addAttribute("soloLectura", false);
        cargarDatosFormulario(usuario, model, actividad);
        return "formulario-actividad";
    }

    // ===== ACTUALIZAR ACTIVIDAD =====
    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute("actividad") Actividad actividadForm,
                             @RequestParam(value = "companerosIds", required = false) List<Long> companerosIds,
                             WebRequest request,
                             RedirectAttributes ra) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        Actividad existente = actividadService.buscarPorId(id);
        if (existente == null || !actividadService.puedeEditar(usuario, existente)) {
            ra.addFlashAttribute("error", "⚠️ No tienes permiso para editar esta actividad.");
            return "redirect:/actividades";
        }

        List<String> errores = actividadService.validarActividad(actividadForm);
        if (!errores.isEmpty()) {
            ra.addFlashAttribute("errorChoque", "⚠️ " + String.join(". ", errores));
            return "redirect:/actividades/editar/" + id;
        }

        // Verificar choques excluyendo la actividad actual
        if (actividadForm.getFechaInicio() != null && actividadForm.getHoraInicio() != null) {
            boolean hayChoque = actividadService.hayChoque(
                    usuario,
                    actividadForm.getFechaInicio(),
                    actividadForm.getHoraInicio(),
                    actividadForm.getDuracionMinutos(),
                    id
            );
            if (hayChoque) {
                ra.addFlashAttribute("errorChoque",
                        "⚠️ Ya tienes una actividad en ese horario. Elige otra hora.");
                return "redirect:/actividades/editar/" + id;
            }
        }

        // Actualizar campos
        existente.setTitulo(actividadForm.getTitulo());
        existente.setDescripcion(actividadForm.getDescripcion());
        existente.setMateria(actividadForm.getMateria());
        existente.setTipo(actividadForm.getTipo());
        existente.setFechaInicio(actividadForm.getFechaInicio());
        existente.setHoraInicio(actividadForm.getHoraInicio());
        existente.setDuracionMinutos(actividadForm.getDuracionMinutos());
        existente.setFechaEntrega(actividadForm.getFechaEntrega());
        existente.setEstado(actividadForm.getEstado());
        existente.setColor(actividadForm.getColor());
        existente.setTiempoPomodoro(actividadForm.getTiempoPomodoro());

        // ===== NUEVO: RECALCULAR PRIORIDAD SI CAMBIÓ LA FECHA DE ENTREGA =====
        if (actividadForm.getFechaEntrega() != null) {
            existente.setPrioridad(actividadService.calcularPrioridadAutomatica(actividadForm));
        } else {
            // Si no hay fecha de entrega, mantener la prioridad que trae el formulario
            existente.setPrioridad(actividadForm.getPrioridad());
        }

        // Revaluar tipo
        String tipo = actividadForm.getTipo();
        if ("CITA_MEDICA".equals(tipo) || "CITA_LABORAL".equals(tipo) || "OTRO".equals(tipo)) {
            existente.setEsAcademico(false);
        } else {
            existente.setEsAcademico(true);
        }

        actividadService.guardar(existente);

        if (esTipoGrupal(existente.getTipo())) {
            actividadCompartidaService.actualizarCompaneros(existente, usuario, companerosIds);
        }

        ra.addFlashAttribute("exito", "✅ Actividad actualizada");
        return "redirect:/dashboard";
    }

    // ===== ELIMINAR ACTIVIDAD =====
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, WebRequest request, RedirectAttributes ra) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        try {
            actividadService.eliminar(id, usuario);
            ra.addFlashAttribute("exito", "Actividad eliminada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/actividades";
    }

    @PostMapping("/api/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> eliminarApi(@PathVariable Long id, WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) {
            return Map.of("ok", false, "mensaje", "Sesión expirada");
        }
        try {
            actividadService.eliminar(id, usuario);
            return Map.of("ok", true, "mensaje", "Actividad eliminada");
        } catch (Exception e) {
            return Map.of("ok", false, "mensaje", e.getMessage());
        }
    }

    // ===== CAMBIAR ESTADO =====
    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String estado,
                                WebRequest request,
                                RedirectAttributes ra) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        Actividad actividad = actividadService.buscarPorId(id);
        if (actividad != null && actividadService.puedeAcceder(usuario, actividad)) {
            actividadService.cambiarEstado(usuario, id, estado);
            ra.addFlashAttribute("exito", "✅ Estado actualizado");
        } else {
            ra.addFlashAttribute("error", "⚠️ No tienes permiso para cambiar el estado.");
        }
        return "redirect:/actividades";
    }

    // ===== REAGENDAR CON VALIDACIÓN DE FECHA =====
    @PostMapping("/reagendar/{id}")
    public String reagendar(@PathVariable Long id,
                            @RequestParam String nuevaFecha,
                            @RequestParam String nuevaHora,
                            WebRequest request,
                            RedirectAttributes ra) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        try {
            LocalDate fecha = LocalDate.parse(nuevaFecha);
            LocalTime hora = LocalTime.parse(nuevaHora);

            // ===== VALIDACIÓN: NO AGENDAR EN PASADO =====
            if (fecha.isBefore(LocalDate.now())) {
                ra.addFlashAttribute("error", "⚠️ No puedes reagendar una tarea para una fecha pasada.");
                return "redirect:/dashboard";
            }

            actividadService.reagendarActividad(usuario, id, fecha, hora);
            ra.addFlashAttribute("exito", "✅ Actividad reagendada exitosamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "⚠️ " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // ===== API: LISTAR REUNIONES/CITAS REAGENDABLES =====
    @GetMapping("/api/reagendables")
    @ResponseBody
    public List<Map<String, Object>> listarReagendables(WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return List.of();
        return actividadService.listarReagendables(usuario).stream()
                .map(actividadService::toReagendarMap)
                .collect(Collectors.toList());
    }

    // ===== API: DETALLE DE ACTIVIDAD =====
    @GetMapping("/api/{id}")
    @ResponseBody
    public Map<String, Object> obtenerDetalle(@PathVariable Long id, WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) {
            return Map.of("ok", false, "mensaje", "Sesión expirada");
        }
        Actividad actividad = actividadService.buscarPorId(id);
        if (actividad == null || !actividadService.puedeAcceder(usuario, actividad)) {
            return Map.of("ok", false, "mensaje", "Actividad no encontrada o sin permiso");
        }
        Map<String, Object> resp = new HashMap<>(actividadService.toDetalleMap(actividad, usuario));
        resp.put("ok", true);
        return resp;
    }

    // ===== API: ACTIVIDADES POR FECHA =====
    @GetMapping("/api/por-fecha")
    @ResponseBody
    public Map<String, Object> listarPorFecha(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                              WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) {
            return Map.of("ok", false, "mensaje", "Sesión expirada");
        }
        List<Map<String, Object>> items = actividadService.listarPorFecha(usuario, fecha).stream()
                .map(actividadService::toResumenMap)
                .collect(Collectors.toList());
        return Map.of("ok", true, "fecha", fecha.toString(), "actividades", items);
    }

    // ===== API: CAMBIAR ESTADO (JSON) =====
    @PostMapping("/api/estado/{id}")
    @ResponseBody
    public Map<String, Object> cambiarEstadoApi(@PathVariable Long id,
                                                @RequestParam String estado,
                                                WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) {
            return Map.of("ok", false, "mensaje", "Sesión expirada");
        }
        try {
            actividadService.cambiarEstado(usuario, id, estado);
            Actividad actividad = actividadService.buscarPorId(id);
            return Map.of("ok", true, "mensaje", "Estado actualizado", "estado", estado,
                    "detalle", actividadService.toDetalleMap(actividad, usuario));
        } catch (Exception e) {
            return Map.of("ok", false, "mensaje", e.getMessage());
        }
    }

    // ===== API: REAGENDAR (JSON, sin recargar página) =====
    @PostMapping("/api/reagendar/{id}")
    @ResponseBody
    public Map<String, Object> reagendarApi(@PathVariable Long id,
                                            @RequestParam String nuevaFecha,
                                            @RequestParam(required = false) String nuevaHora,
                                            WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) {
            return Map.of("ok", false, "mensaje", "Sesión expirada");
        }
        try {
            LocalDate fecha = LocalDate.parse(nuevaFecha);
            LocalTime hora = (nuevaHora != null && !nuevaHora.isBlank())
                    ? LocalTime.parse(nuevaHora) : null;
            Actividad actualizada = actividadService.reagendarActividad(usuario, id, fecha, hora);
            return Map.of(
                    "ok", true,
                    "mensaje", "Actividad reagendada para " + fecha + " a las " + hora,
                    "actividad", actividadService.toReagendarMap(actualizada)
            );
        } catch (Exception e) {
            return Map.of("ok", false, "mensaje", e.getMessage());
        }
    }
}