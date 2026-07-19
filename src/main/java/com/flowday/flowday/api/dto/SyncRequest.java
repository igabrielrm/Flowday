package com.flowday.flowday.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SyncRequest(
        @NotBlank @Size(max = 200) String deviceId,
        @NotEmpty @Size(max = 100) List<@Valid SyncOperationRequest> operations
) {}
