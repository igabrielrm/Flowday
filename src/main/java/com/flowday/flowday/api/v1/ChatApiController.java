package com.flowday.flowday.api.v1;

import com.flowday.flowday.api.ApiAuthHelper;
import com.flowday.flowday.api.dto.ApiResponse;
import com.flowday.flowday.api.dto.ChatMessageDto;
import com.flowday.flowday.api.dto.ConversationDto;
import com.flowday.flowday.api.dto.SendChatMessageRequest;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.service.ChatService;
import com.flowday.flowday.service.ConexionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "Mensajes privados entre usuarios conectados")
public class ChatApiController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ConexionService conexionService;

    @GetMapping("/conversations")
    @Operation(summary = "Listar conversaciones con compañeros conectados")
    public ApiResponse<List<ConversationDto>> conversations(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        List<ConversationDto> items = chatService.listarConversaciones(
                usuario, conexionService.obtenerCompanerosConectados(usuario));
        return ApiResponse.success(items);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Mensajes no leídos totales")
    public ApiResponse<Map<String, Long>> unreadCount(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        return ApiResponse.success(Map.of("count", chatService.contarNoLeidos(usuario)));
    }

    @GetMapping("/messages/{userId}")
    @Operation(summary = "Mensajes con un usuario")
    public ApiResponse<List<ChatMessageDto>> messages(@PathVariable Long userId, WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        try {
            return ApiResponse.success(chatService.listarConversacion(usuario, userId));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }

    @PostMapping("/messages")
    @Operation(summary = "Enviar mensaje")
    public ApiResponse<ChatMessageDto> send(@Valid @RequestBody SendChatMessageRequest body,
                                            WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        try {
            return ApiResponse.success(chatService.enviar(usuario, body.destinatarioId(), body.contenido()));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }

    @PostMapping("/messages/{userId}/read")
    @Operation(summary = "Marcar mensajes como leídos")
    public ApiResponse<Map<String, Integer>> markRead(@PathVariable Long userId, WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        try {
            return ApiResponse.success(Map.of("updated", chatService.marcarLeidos(usuario, userId)));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }

    @DeleteMapping("/conversations/{userId}")
    @Operation(summary = "Eliminar historial de conversación")
    public ApiResponse<Map<String, Integer>> deleteConversation(@PathVariable Long userId, WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        try {
            return ApiResponse.success(Map.of("deleted", chatService.eliminarConversacion(usuario, userId)));
        } catch (IllegalArgumentException ex) {
            return ApiResponse.failure(ex.getMessage());
        }
    }
}
