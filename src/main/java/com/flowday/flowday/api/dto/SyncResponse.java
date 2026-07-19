package com.flowday.flowday.api.dto;

import java.util.List;

public record SyncResponse(
        String deviceId,
        List<SyncOperationResult> results
) {}
