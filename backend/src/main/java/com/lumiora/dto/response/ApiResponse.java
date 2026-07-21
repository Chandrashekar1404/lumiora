package com.lumiora.dto.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean success;

    private final String message;

    private final T data;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ApiResponse<Void> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
