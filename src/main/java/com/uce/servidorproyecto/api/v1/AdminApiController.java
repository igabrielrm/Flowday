package com.uce.servidorproyecto.api.v1;

import com.uce.servidorproyecto.api.ApiAuthHelper;
import com.uce.servidorproyecto.api.dto.ApiResponse;
import com.uce.servidorproyecto.model.Anuncio;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.AnuncioRepository;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import com.uce.servidorproyecto.service.AdminAnalyticsService;
import com.uce.servidorproyecto.service.AdminService;
import com.uce.servidorproyecto.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Panel de administración")
public class AdminApiController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminAnalyticsService adminAnalyticsService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AnuncioRepository anuncioRepository;

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping("/stats")
    @Operation(summary = "Estadísticas generales del panel")
    public ApiResponse<Map<String, Object>> stats(WebRequest request) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminService.getEstadisticasGenerales());
    }

    @GetMapping("/wellbeing")
    @Operation(summary = "Monitoreo de bienestar")
    public ApiResponse<Map<String, Object>> wellbeing(WebRequest request) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminService.getMonitoreoBienestar());
    }

    @GetMapping("/users")
    @Operation(summary = "Listar usuarios")
    public ApiResponse<List<Map<String, Object>>> users(WebRequest request) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        List<Map<String, Object>> users = usuarioRepository.findAll().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("nombre", u.getNombre());
            m.put("correo", u.getCorreo());
            m.put("rol", u.getRol());
            m.put("rolDisplay", u.getRolDisplay());
            return m;
        }).toList();
        return ApiResponse.success(users);
    }

    @GetMapping("/users/top")
    @Operation(summary = "Top usuarios por productividad")
    public ApiResponse<List<Map<String, Object>>> topUsers(
            @RequestParam(defaultValue = "8") int limit, WebRequest request) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminService.getTopUsuarios(limit));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Alternar rol USER/ADMIN")
    public ApiResponse<Map<String, Object>> toggleRole(@PathVariable Long id, WebRequest request) {
        Usuario admin = ApiAuthHelper.requireAdmin(request);
        if (admin == null) return ApiResponse.failure("No autorizado");
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) return ApiResponse.failure("Usuario no encontrado");
        if (usuario.getId().equals(admin.getId())) {
            return ApiResponse.failure("No puedes cambiar tu propio rol");
        }
        usuario.setRol("ADMIN".equals(usuario.getRol()) ? "USER" : "ADMIN");
        usuarioRepository.save(usuario);
        return ApiResponse.success(Map.of("id", usuario.getId(), "rol", usuario.getRol()));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Eliminar usuario")
    public ApiResponse<Map<String, Object>> deleteUser(@PathVariable Long id, WebRequest request) {
        Usuario admin = ApiAuthHelper.requireAdmin(request);
        if (admin == null) return ApiResponse.failure("No autorizado");
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) return ApiResponse.failure("Usuario no encontrado");
        if ("ADMIN".equals(usuario.getRol())) return ApiResponse.failure("No se puede eliminar un administrador");
        if (usuario.getId().equals(admin.getId())) return ApiResponse.failure("No puedes eliminarte a ti mismo");
        usuarioRepository.deleteById(id);
        return ApiResponse.success(Map.of("ok", true));
    }

    @GetMapping("/announcements")
    @Operation(summary = "Anuncios activos y archivados")
    public ApiResponse<Map<String, Object>> announcements(WebRequest request) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        List<Anuncio> all = anuncioRepository.findAllByOrderByFechaLimiteDesc();
        List<Map<String, Object>> activos = all.stream()
                .filter(a -> "ACTIVO".equals(a.getEstado()))
                .map(this::toAnnouncementMap)
                .toList();
        List<Map<String, Object>> archivados = all.stream()
                .filter(a -> "ARCHIVADO".equals(a.getEstado()))
                .map(this::toAnnouncementMap)
                .toList();
        return ApiResponse.success(Map.of("activos", activos, "archivados", archivados));
    }

    @PostMapping("/announcements")
    @Operation(summary = "Publicar anuncio global")
    public ApiResponse<Map<String, Object>> createAnnouncement(
            @RequestBody Map<String, String> body, WebRequest request) {
        Usuario admin = ApiAuthHelper.requireAdmin(request);
        if (admin == null) return ApiResponse.failure("No autorizado");
        String titulo = body.get("titulo");
        String descripcion = body.get("descripcion");
        String fechaLimite = body.get("fechaLimite");
        if (titulo == null || titulo.isBlank() || fechaLimite == null || fechaLimite.isBlank()) {
            return ApiResponse.failure("Título y fecha límite son obligatorios");
        }
        Anuncio anuncio = new Anuncio();
        anuncio.setTitulo(titulo.trim());
        anuncio.setDescripcion(descripcion != null ? descripcion.trim() : "");
        anuncio.setFechaLimite(LocalDate.parse(fechaLimite));
        anuncio.setCreador(admin);
        Anuncio guardado = anuncioRepository.save(anuncio);
        notificacionService.notificarAnuncioGlobal(guardado);
        return ApiResponse.success(toAnnouncementMap(guardado));
    }

    @PostMapping("/announcements/{id}/archive")
    public ApiResponse<Map<String, Object>> archiveAnnouncement(@PathVariable Long id, WebRequest request) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        Anuncio anuncio = anuncioRepository.findById(id).orElse(null);
        if (anuncio == null) return ApiResponse.failure("Anuncio no encontrado");
        anuncio.setEstado("ARCHIVADO");
        anuncioRepository.save(anuncio);
        return ApiResponse.success(toAnnouncementMap(anuncio));
    }

    @PostMapping("/announcements/{id}/restore")
    public ApiResponse<Map<String, Object>> restoreAnnouncement(@PathVariable Long id, WebRequest request) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        Anuncio anuncio = anuncioRepository.findById(id).orElse(null);
        if (anuncio == null) return ApiResponse.failure("Anuncio no encontrado");
        anuncio.setEstado("ACTIVO");
        anuncioRepository.save(anuncio);
        return ApiResponse.success(toAnnouncementMap(anuncio));
    }

    @DeleteMapping("/announcements/{id}")
    public ApiResponse<Map<String, Object>> deleteAnnouncement(@PathVariable Long id, WebRequest request) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        if (!anuncioRepository.existsById(id)) return ApiResponse.failure("Anuncio no encontrado");
        anuncioRepository.deleteById(id);
        return ApiResponse.success(Map.of("ok", true));
    }

    // ===== Analytics (delegación a AdminAnalyticsService) =====

    @GetMapping("/analytics/resumen")
    public ApiResponse<Map<String, Object>> analyticsResumen(
            WebRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminAnalyticsService.getResumen(desde, hasta));
    }

    @GetMapping("/analytics/stress-by-cohort")
    public ApiResponse<Map<String, Object>> analyticsStress(
            WebRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminAnalyticsService.getEstresPorCarrera(desde, hasta));
    }

    @GetMapping("/analytics/activities-by-cohort")
    public ApiResponse<Map<String, Object>> analyticsActivitiesCohort(
            WebRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminAnalyticsService.getActividadesPorCarrera(desde, hasta));
    }

    @GetMapping("/analytics/reschedules")
    public ApiResponse<Map<String, Object>> analyticsReschedules(
            WebRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminAnalyticsService.getReagendamientos(desde, hasta));
    }

    @GetMapping("/analytics/wellbeing")
    public ApiResponse<Map<String, Object>> analyticsWellbeing(
            WebRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminAnalyticsService.getBienestar(desde, hasta));
    }

    @GetMapping("/analytics/activities-by-type")
    public ApiResponse<Map<String, Object>> analyticsByType(
            WebRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminAnalyticsService.getActividadesPorTipo(desde, hasta));
    }

    @GetMapping("/analytics/activities-by-day")
    public ApiResponse<Map<String, Object>> analyticsByDay(
            WebRequest request, @RequestParam(defaultValue = "7") int dias) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminAnalyticsService.getActividadesPorDia(dias));
    }

    @GetMapping("/analytics/critical-weeks")
    public ApiResponse<Map<String, Object>> analyticsCriticalWeeks(WebRequest request) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminAnalyticsService.getSemanasCriticas());
    }

    @GetMapping("/analytics/user-status")
    public ApiResponse<Map<String, Object>> analyticsUserStatus(WebRequest request) {
        if (ApiAuthHelper.requireAdmin(request) == null) return ApiResponse.failure("No autorizado");
        return ApiResponse.success(adminAnalyticsService.getEstadoUsuarios());
    }

    private Map<String, Object> toAnnouncementMap(Anuncio a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("titulo", a.getTitulo());
        m.put("descripcion", a.getDescripcion());
        m.put("fechaLimite", a.getFechaLimite() != null ? a.getFechaLimite().toString() : null);
        m.put("fechaPublicacion", a.getFechaPublicacion() != null ? a.getFechaPublicacion().toString() : null);
        m.put("estado", a.getEstado());
        return m;
    }
}
