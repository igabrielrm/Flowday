package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.api.ApiAuthHelper;
import com.uce.servidorproyecto.model.Actividad;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.service.ActividadService;
import com.uce.servidorproyecto.service.GroqService;
import com.uce.servidorproyecto.service.IAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ia")
public class IAController {

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private IAService iaService;

    @Autowired
    private GroqService groqService;

    @Value("${ia.provider:groq}")
    private String iaProvider;

    @GetMapping("/status")
    public Map<String, Object> status(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) {
            return Map.of("ok", false, "error", "No autenticado");
        }
        boolean groqReady = groqService.estaConfigurado();
        return Map.of(
                "ok", true,
                "provider", iaProvider,
                "groqConfigured", groqReady,
                "ready", groqReady || "auto".equalsIgnoreCase(iaProvider)
        );
    }

    // ===== REAGENDAR INTELIGENTE =====
    @PostMapping("/reagendar")
    public Map<String, Object> reagendarInteligente(@RequestBody(required = false) Map<String, Object> filtros,
                                                    WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) {
            return Map.of("error", "Usuario no autenticado");
        }

        List<Actividad> actividades = actividadService.listarPorUsuario(usuario);
        List<Actividad> pendientes = actividades.stream()
                .filter(a -> !"COMPLETADA".equals(a.getEstado()))
                .collect(Collectors.toList());

        if (pendientes.isEmpty()) {
            return Map.of("mensaje", "🎉 No tienes actividades pendientes. ¡Buen trabajo!");
        }

        Map<String, Object> resultado = iaService.optimizarHorario(pendientes, usuario);

        if (filtros != null && "true".equals(filtros.get("aplicar"))) {
            iaService.aplicarOptimizacion(pendientes, resultado);
        }

        return resultado;
    }

    // ===== MODO AUXILIO DE ESTUDIO =====
    @PostMapping("/auxilio")
    public Map<String, Object> modoAuxilio(@RequestParam String tema,
                                           @RequestParam(required = false) String tipo,
                                           WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) {
            return Map.of("error", "Usuario no autenticado");
        }

        return iaService.generarRecursosEstudio(tema, tipo);
    }

    // ===== DETECTAR BLOQUEO CREATIVO =====
    @PostMapping("/detectar-bloqueo")
    public Map<String, Object> detectarBloqueo(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) {
            return Map.of("error", "Usuario no autenticado");
        }

        List<Actividad> actividades = actividadService.listarPorUsuario(usuario);
        List<Actividad> recientes = actividades.stream()
                .filter(a -> a.getFechaInicio() != null &&
                            a.getFechaInicio().isAfter(LocalDate.now().minusDays(3)))
                .collect(Collectors.toList());

        return iaService.detectarBloqueo(recientes);
    }

    // ===== RECURSOS POR TEMA =====
    @GetMapping("/recursos")
    public Map<String, Object> sugerirRecursos(@RequestParam String tema,
                                               @RequestParam(required = false) Integer cantidad,
                                               WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) {
            return Map.of("error", "Usuario no autenticado");
        }

        return iaService.generarRecursosEstudio(tema, cantidad != null ? cantidad.toString() : "5");
    }

    // ===== SUGERIR PAUSA ACTIVA =====
    @GetMapping("/pausa")
    public Map<String, String> sugerirPausa(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) {
            return Map.of("error", "Usuario no autenticado");
        }
        return iaService.sugerirPausaActiva();
    }

    // ===== CHAT COMPAÑERO VIRTUAL =====
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> body, WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) {
            return Map.of("ok", false, "mensaje", "Sesión expirada");
        }
        String mensaje = body != null && body.get("mensaje") != null ? body.get("mensaje").toString() : null;
        if (mensaje == null || mensaje.isBlank()) {
            return Map.of("ok", false, "mensaje", "Escribe un mensaje");
        }
        if (mensaje.length() > 500) {
            mensaje = mensaje.substring(0, 500);
        }
        List<Map<String, String>> historial = extraerHistorialChat(body);
        return iaService.chatCompanero(mensaje.trim(), usuario, historial);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> extraerHistorialChat(Map<String, Object> body) {
        if (body == null || !(body.get("historial") instanceof List<?> raw)) {
            return List.of();
        }
        List<Map<String, String>> historial = new ArrayList<>();
        for (Object item : raw) {
            if (!(item instanceof Map<?, ?> map)) continue;
            Object roleObj = map.get("role");
            Object textObj = map.get("text");
            if (roleObj == null || textObj == null) continue;
            String role = roleObj.toString().trim();
            String text = textObj.toString().trim();
            if (text.isEmpty() || "loading".equals(role)) continue;
            if (text.length() > 400) text = text.substring(0, 400);
            historial.add(Map.of("role", role, "text", text));
        }
        if (historial.size() > 12) {
            return new ArrayList<>(historial.subList(historial.size() - 12, historial.size()));
        }
        return historial;
    }

    // ===== REAGENDAR EN CRISIS =====
    @PostMapping("/reagendar-crisis")
    public Map<String, Object> reagendarCrisis(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) {
            return Map.of("error", "Usuario no autenticado");
        }

        List<Actividad> actividades = actividadService.listarPorUsuario(usuario);
        List<Actividad> pendientes = actividades.stream()
                .filter(a -> !"COMPLETADA".equals(a.getEstado()))
                .sorted((a, b) -> {
                    int prioridadA = "ALTA".equals(a.getPrioridad()) ? 3 :
                                     "MEDIA".equals(a.getPrioridad()) ? 2 : 1;
                    int prioridadB = "ALTA".equals(b.getPrioridad()) ? 3 :
                                     "MEDIA".equals(b.getPrioridad()) ? 2 : 1;
                    return Integer.compare(prioridadB, prioridadA);
                })
                .collect(Collectors.toList());

        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("titulo", "⚡ Plan de Crisis - Optimización Rápida");
        plan.put("totalPendientes", pendientes.size());

        List<Map<String, String>> pasos = new ArrayList<>();
        LocalTime hora = LocalTime.of(8, 0);

        for (int i = 0; i < Math.min(pendientes.size(), 6); i++) {
            Actividad a = pendientes.get(i);
            Map<String, String> paso = new LinkedHashMap<>();
            paso.put("hora", hora.toString());
            paso.put("titulo", a.getTitulo());
            paso.put("prioridad", a.getPrioridad());
            if (a.getDuracionMinutos() != null) {
                paso.put("duracion", a.getDuracionMinutos() + " min");
                hora = hora.plusMinutes(a.getDuracionMinutos() + 5);
            }
            pasos.add(paso);
        }

        plan.put("pasos", pasos);
        plan.put("mensaje", "✅ Plan generado. Prioriza las tareas de ALTA prioridad.");

        if (pendientes.size() > 6) {
            plan.put("advertencia", "⚠️ Tienes " + pendientes.size() + " tareas. Considera mover algunas a mañana.");
        }

        return plan;
    }
}
