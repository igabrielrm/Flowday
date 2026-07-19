package com.flowday.flowday.api.v1;

import com.flowday.flowday.api.ApiAuthHelper;
import com.flowday.flowday.api.dto.SyncRequest;
import com.flowday.flowday.api.dto.SyncResponse;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/sync")
@Tag(name = "Sync", description = "Sincronización idempotente de operaciones offline")
public class SyncApiController {

    private final SyncService syncService;

    public SyncApiController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Aplicar un batch de operaciones offline independientes")
    public SyncResponse sync(@Valid @RequestBody SyncRequest body, WebRequest request) {
        Usuario user = ApiAuthHelper.requireUser(request);
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        return syncService.process(user, body);
    }
}
