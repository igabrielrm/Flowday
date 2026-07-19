package com.flowday.flowday.api.v1;

import com.flowday.flowday.api.ApiAuthHelper;
import com.flowday.flowday.api.dto.ApiResponse;
import com.flowday.flowday.api.dto.CreateScheduleBlockRequest;
import com.flowday.flowday.api.dto.ScheduleBlockDto;
import com.flowday.flowday.model.BloqueRecurrente;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.service.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/schedule")
@Tag(name = "Schedule", description = "Horario semanal de clases")
public class ScheduleApiController {

    @Autowired
    private HorarioService horarioService;

    @GetMapping("/config")
    @Operation(summary = "Configuración del grid de horario")
    public ApiResponse<Map<String, Integer>> config() {
        Map<String, Integer> cfg = new LinkedHashMap<>();
        cfg.put("gridStart", HorarioService.HORA_GRID_INICIO);
        cfg.put("gridEnd", HorarioService.HORA_GRID_FIN);
        cfg.put("slotMinutes", 60);
        return ApiResponse.success(cfg);
    }

    @GetMapping("/blocks")
    @Operation(summary = "Listar bloques del horario semanal")
    public ApiResponse<List<ScheduleBlockDto>> list(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        List<ScheduleBlockDto> items = horarioService.listarPorUsuario(usuario).stream()
                .map(this::toDto)
                .toList();
        return ApiResponse.success(items, Map.of("total", items.size()));
    }

    @GetMapping("/alert")
    @Operation(summary = "Alerta de bloque actual o próximo")
    public ApiResponse<Map<String, Object>> alert(
            @RequestParam(defaultValue = "15") int minutesBefore,
            WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        return horarioService.obtenerAlertaBloque(usuario, minutesBefore)
                .map(ApiResponse::<Map<String, Object>>success)
                .orElse(ApiResponse.success(null));
    }

    @PostMapping("/blocks")
    @Operation(summary = "Crear bloque de horario")
    public ApiResponse<ScheduleBlockDto> create(@Valid @RequestBody CreateScheduleBlockRequest body,
                                                WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        try {
            BloqueRecurrente guardado = horarioService.guardar(usuario, fromRequest(body));
            return ApiResponse.success(toDto(guardado));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }

    @PutMapping("/blocks/{id}")
    @Operation(summary = "Actualizar bloque de horario")
    public ApiResponse<ScheduleBlockDto> update(@PathVariable Long id,
                                                @Valid @RequestBody CreateScheduleBlockRequest body,
                                                WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        try {
            BloqueRecurrente actualizado = horarioService.actualizar(usuario, id, fromRequest(body));
            return ApiResponse.success(toDto(actualizado));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }

    @DeleteMapping("/blocks/{id}")
    @Operation(summary = "Eliminar bloque de horario")
    public ApiResponse<Void> delete(@PathVariable Long id, WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        try {
            horarioService.eliminar(usuario, id);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }

    private ScheduleBlockDto toDto(BloqueRecurrente c) {
        Map<String, Object> map = horarioService.toMap(c);
        return new ScheduleBlockDto(
                (Long) map.get("id"),
                c.getVersion(),
                (String) map.get("materia"),
                (Integer) map.get("diaSemana"),
                (String) map.get("diaNombre"),
                (String) map.get("horaInicio"),
                (String) map.get("horaFin"),
                (String) map.get("aula"),
                (String) map.get("profesor"),
                (String) map.get("color")
        );
    }

    private BloqueRecurrente fromRequest(CreateScheduleBlockRequest body) {
        BloqueRecurrente b = new BloqueRecurrente();
        b.setMateria(body.materia().trim());
        b.setDiaSemana(body.diaSemana());
        b.setHoraInicio(body.horaInicio());
        b.setHoraFin(body.horaFin());
        b.setAula(body.aula());
        b.setProfesor(body.profesor());
        b.setColor(body.color());
        return b;
    }
}
