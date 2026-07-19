package com.flowday.flowday.api.v1;

import com.flowday.flowday.api.ApiAuthHelper;
import com.flowday.flowday.api.dto.ApiResponse;
import com.flowday.flowday.api.dto.CommunityStatsDto;
import com.flowday.flowday.api.dto.CommunityUserDto;
import com.flowday.flowday.api.dto.ConnectUserRequest;
import com.flowday.flowday.api.dto.UsuarioDto;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.service.ComunidadService;
import com.flowday.flowday.service.ConexionService;
import com.flowday.flowday.service.ConexionService.RelacionInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/community")
@Tag(name = "Community", description = "Comunidad y conexiones entre usuarios")
public class CommunityApiController {

    @Autowired
    private ComunidadService comunidadService;

    @Autowired
    private ConexionService conexionService;

    @GetMapping("/stats")
    @Operation(summary = "Estadísticas de la comunidad")
    public ApiResponse<CommunityStatsDto> stats(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");

        Map<String, Object> raw = comunidadService.getEstadisticasComunidad();
        CommunityStatsDto dto = new CommunityStatsDto(
                ((Number) raw.getOrDefault("totalUsuarios", 0L)).longValue(),
                ((Number) raw.getOrDefault("totalGrupos", 0L)).longValue(),
                ((Number) raw.getOrDefault("tasaConexion", 0)).intValue()
        );
        return ApiResponse.success(dto);
    }

    @GetMapping("/users")
    @Operation(summary = "Buscar usuarios de la comunidad")
    public ApiResponse<List<CommunityUserDto>> users(
            @RequestParam(required = false) String query,
            WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");

        Map<Long, Integer> compatById = compatibilidadPorId(usuario);

        List<CommunityUserDto> items = comunidadService.buscarCompaneros(query).stream()
                .filter(u -> !u.getId().equals(usuario.getId()))
                .map(u -> toCommunityUser(u, compatById, usuario))
                .toList();
        return ApiResponse.success(items);
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Sugerencias de compañeros")
    public ApiResponse<List<CommunityUserDto>> suggestions(
            @RequestParam(defaultValue = "4") int limit,
            WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");

        Map<Long, Integer> compatById = compatibilidadPorId(usuario);

        List<CommunityUserDto> items = comunidadService.sugerirGrupos(usuario).stream()
                .limit(Math.max(1, Math.min(limit, 12)))
                .map(u -> toCommunityUser(u, compatById, usuario))
                .toList();
        return ApiResponse.success(items);
    }

    @GetMapping("/connections")
    @Operation(summary = "Compañeros conectados")
    public ApiResponse<List<UsuarioDto>> connections(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        List<UsuarioDto> items = conexionService.obtenerCompanerosConectados(usuario).stream()
                .map(UsuarioDto::from)
                .toList();
        return ApiResponse.success(items);
    }

    @PostMapping("/connections")
    @Operation(summary = "Enviar solicitud de amistad")
    public ApiResponse<Map<String, String>> connect(@Valid @RequestBody ConnectUserRequest body,
                                                    WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");

        try {
            conexionService.solicitarConexion(usuario, body.userId());
            return ApiResponse.success(Map.of("mensaje", "Solicitud enviada correctamente"));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }

    @PostMapping("/connections/{id}/accept")
    @Operation(summary = "Aceptar solicitud de amistad")
    public ApiResponse<Map<String, String>> accept(@PathVariable Long id, WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        try {
            conexionService.aceptarSolicitud(usuario, id);
            return ApiResponse.success(Map.of("mensaje", "Solicitud aceptada"));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }

    @PostMapping("/connections/{id}/reject")
    @Operation(summary = "Rechazar solicitud de amistad")
    public ApiResponse<Map<String, String>> reject(@PathVariable Long id, WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        try {
            conexionService.rechazarSolicitud(usuario, id);
            return ApiResponse.success(Map.of("mensaje", "Solicitud rechazada"));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }

    @DeleteMapping("/connections/{id}")
    @Operation(summary = "Cancelar solicitud o desconectar")
    public ApiResponse<Void> remove(@PathVariable Long id, WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        try {
            RelacionInfo rel = conexionService.obtenerRelacionPorId(usuario, id);
            if ("SOLICITUD_ENVIADA".equals(rel.estadoRelacion())) {
                conexionService.cancelarSolicitud(usuario, id);
            } else if ("CONECTADO".equals(rel.estadoRelacion())) {
                conexionService.desconectar(usuario, id);
            } else {
                return ApiResponse.failure("No puedes cancelar esta solicitud");
            }
            return ApiResponse.success(null);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }

    private Map<Long, Integer> compatibilidadPorId(Usuario usuario) {
        return comunidadService.calcularCompatibilidad(usuario).entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        e -> e.getKey().getId(),
                        Map.Entry::getValue,
                        (a, b) -> a
                ));
    }

    private CommunityUserDto toCommunityUser(Usuario u, Map<Long, Integer> compatById, Usuario yo) {
        RelacionInfo rel = conexionService.obtenerRelacion(yo, u);
        return new CommunityUserDto(
                UsuarioDto.from(u),
                compatById.getOrDefault(u.getId(), 0),
                rel.conectado(),
                rel.estadoRelacion(),
                rel.conexionId()
        );
    }
}
