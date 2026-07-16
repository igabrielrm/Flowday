package com.uce.servidorproyecto.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uce.servidorproyecto.api.dto.SyncOperationRequest;
import com.uce.servidorproyecto.api.dto.SyncOperationResult;
import com.uce.servidorproyecto.api.dto.SyncRequest;
import com.uce.servidorproyecto.api.dto.SyncResponse;
import com.uce.servidorproyecto.model.SyncOperation;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.SyncOperationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SyncService {

    private final SyncOperationRepository syncOperationRepository;
    private final SyncDomainService syncDomainService;
    private final ObjectMapper mapper;
    private final TransactionTemplate requiresNew;

    public SyncService(SyncOperationRepository syncOperationRepository,
                       SyncDomainService syncDomainService,
                       ObjectMapper mapper,
                       PlatformTransactionManager transactionManager) {
        this.syncOperationRepository = syncOperationRepository;
        this.syncDomainService = syncDomainService;
        this.mapper = mapper;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public SyncResponse process(Usuario user, SyncRequest request) {
        List<SyncOperationResult> results = new ArrayList<>(request.operations().size());
        for (SyncOperationRequest operation : request.operations()) {
            results.add(processOne(user, request.deviceId().trim(), operation));
        }
        return new SyncResponse(request.deviceId().trim(), results);
    }

    /*
     * The database constraint is the cross-process guarantee. Synchronization also avoids
     * needless unique-key races when retries arrive concurrently in this JVM.
     */
    public synchronized SyncOperationResult processOne(
            Usuario user, String deviceId, SyncOperationRequest request) {
        try {
            return requiresNew.execute(status -> executeTransaction(user, deviceId, request));
        } catch (DataIntegrityViolationException ex) {
            return requiresNew.execute(status -> duplicateResult(user, deviceId, request));
        } catch (Exception ex) {
            return persistRejected(user, deviceId, request, rootMessage(ex));
        }
    }

    private SyncOperationResult executeTransaction(
            Usuario user, String deviceId, SyncOperationRequest request) {
        var existing = syncOperationRepository.findByUsuarioIdAndDeviceIdAndOperationId(
                user.getId(), deviceId, request.operationId());
        if (existing.isPresent()) return duplicate(existing.get(), request);

        SyncOperation stored = new SyncOperation();
        stored.setUsuario(user);
        stored.setDeviceId(deviceId);
        stored.setOperationId(request.operationId());
        stored.setKind(request.kind());
        stored.setStatus("PROCESSING");
        syncOperationRepository.saveAndFlush(stored);

        try {
            SyncDomainService.Outcome outcome = syncDomainService.apply(user, request);
            complete(stored, outcome.status(), outcome.data(), outcome.error(), outcome.serverVersion());
            syncOperationRepository.save(stored);
            return result(request, outcome.status(), outcome.data(), outcome.error(), outcome.serverVersion());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            String error = rootMessage(ex);
            complete(stored, "REJECTED", null, error, null);
            syncOperationRepository.save(stored);
            return result(request, "REJECTED", null, error, null);
        }
    }

    private SyncOperationResult persistRejected(
            Usuario user, String deviceId, SyncOperationRequest request, String error) {
        try {
            return requiresNew.execute(status -> {
                var existing = syncOperationRepository.findByUsuarioIdAndDeviceIdAndOperationId(
                        user.getId(), deviceId, request.operationId());
                if (existing.isPresent()) return duplicate(existing.get(), request);
                SyncOperation stored = new SyncOperation();
                stored.setUsuario(user);
                stored.setDeviceId(deviceId);
                stored.setOperationId(request.operationId());
                stored.setKind(request.kind());
                complete(stored, "REJECTED", null, error, null);
                syncOperationRepository.saveAndFlush(stored);
                return result(request, "REJECTED", null, error, null);
            });
        } catch (DataIntegrityViolationException race) {
            return requiresNew.execute(status -> duplicateResult(user, deviceId, request));
        }
    }

    private SyncOperationResult duplicateResult(
            Usuario user, String deviceId, SyncOperationRequest request) {
        SyncOperation existing = syncOperationRepository.findByUsuarioIdAndDeviceIdAndOperationId(
                        user.getId(), deviceId, request.operationId())
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la operación duplicada"));
        return duplicate(existing, request);
    }

    private SyncOperationResult duplicate(SyncOperation stored, SyncOperationRequest request) {
        return result(request, "DUPLICATE", parse(stored.getResponseJson()),
                stored.getError(), stored.getServerVersion());
    }

    private void complete(SyncOperation stored, String status, JsonNode data, String error, Long version) {
        stored.setStatus(status);
        stored.setResponseJson(write(data));
        stored.setError(limit(error, 1000));
        stored.setServerVersion(version);
        stored.setCompletedAt(LocalDateTime.now());
    }

    private SyncOperationResult result(SyncOperationRequest request, String status, JsonNode data,
                                       String error, Long version) {
        return new SyncOperationResult(request.operationId(), request.kind(), status,
                request.localEntityId(), data, error, version);
    }

    private String write(JsonNode data) {
        if (data == null || data.isNull()) return null;
        try {
            return mapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo serializar el resultado", ex);
        }
    }

    private JsonNode parse(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return mapper.readTree(json);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo leer el resultado almacenado", ex);
        }
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        String message = current.getMessage();
        return message == null || message.isBlank() ? "Operación rechazada" : message;
    }

    private static String limit(String value, int max) {
        return value != null && value.length() > max ? value.substring(0, max) : value;
    }
}
