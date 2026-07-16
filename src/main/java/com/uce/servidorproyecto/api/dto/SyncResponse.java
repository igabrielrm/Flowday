package com.uce.servidorproyecto.api.dto;

import java.util.List;

public record SyncResponse(
        String deviceId,
        List<SyncOperationResult> results
) {}
