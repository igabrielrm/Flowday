package com.uce.servidorproyecto.api.v1;

import com.uce.servidorproyecto.api.ApiAuthHelper;
import com.uce.servidorproyecto.api.dto.ApiResponse;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notificaciones del usuario")
public class NotificationApiController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    @Operation(summary = "Listar notificaciones recientes")
    public ApiResponse<List<Map<String, Object>>> list(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        return ApiResponse.success(notificacionService.listarRecientes(usuario));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Contador de no leídas")
    public ApiResponse<Map<String, Long>> unreadCount(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        return ApiResponse.success(Map.of("count", notificacionService.contarNoLeidas(usuario)));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Marcar notificación como leída")
    public ApiResponse<Map<String, Object>> markRead(@PathVariable Long id, WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        boolean ok = notificacionService.marcarLeida(id, usuario);
        return ApiResponse.success(Map.of(
                "ok", ok,
                "count", notificacionService.contarNoLeidas(usuario)
        ));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    public ApiResponse<Map<String, Object>> markAllRead(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        int updated = notificacionService.marcarTodasLeidas(usuario);
        return ApiResponse.success(Map.of("ok", true, "count", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar notificación")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id, WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        boolean ok = notificacionService.eliminar(id, usuario);
        if (!ok) return ApiResponse.failure("Notificación no encontrada");
        return ApiResponse.success(Map.of(
                "ok", true,
                "count", notificacionService.contarNoLeidas(usuario)
        ));
    }
}
