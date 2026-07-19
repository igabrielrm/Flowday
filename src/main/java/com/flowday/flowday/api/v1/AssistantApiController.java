package com.flowday.flowday.api.v1;

import com.flowday.flowday.api.ApiAuthHelper;
import com.flowday.flowday.api.dto.AssistantMessageRequest;
import com.flowday.flowday.api.dto.AssistantMessageResponse;
import com.flowday.flowday.api.dto.AssistantProposalDto;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantApiController {

    private final AssistantService assistantService;

    public AssistantApiController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/messages")
    public ResponseEntity<?> message(@Valid @RequestBody AssistantMessageRequest body, WebRequest request) {
        Usuario user = ApiAuthHelper.requireUser(request);
        if (user == null) return unauthorized();
        try {
            AssistantMessageResponse response = assistantService.message(user, body);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/actions/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable UUID id, WebRequest request) {
        Usuario user = ApiAuthHelper.requireUser(request);
        if (user == null) return unauthorized();
        try {
            AssistantProposalDto response = assistantService.confirm(user, id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/actions/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable UUID id, WebRequest request) {
        Usuario user = ApiAuthHelper.requireUser(request);
        if (user == null) return unauthorized();
        try {
            return ResponseEntity.ok(assistantService.cancel(user, id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
    }
}
