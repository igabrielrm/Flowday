package com.flowday.flowday.api.dto;

import jakarta.validation.constraints.NotNull;

public record ConnectUserRequest(@NotNull Long userId) {}
