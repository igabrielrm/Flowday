package com.uce.servidorproyecto.api.dto;

import java.util.Map;

public record ApiResponse<T>(boolean ok, T data, String error, Map<String, Object> meta) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, Map<String, Object> meta) {
        return new ApiResponse<>(true, data, null, meta);
    }

    public static <T> ApiResponse<T> failure(String error) {
        return new ApiResponse<>(false, null, error, null);
    }
}
