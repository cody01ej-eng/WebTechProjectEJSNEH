package com.tcu.projectpulse.shared.api;

public record Result<T>(boolean flag, int code, String message, T data) {

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, StatusCode.SUCCESS, message, data);
    }

    public static Result<Void> success(String message) {
        return success(message, null);
    }

    public static <T> Result<T> failure(int code, String message) {
        return new Result<>(false, code, message, null);
    }
}
