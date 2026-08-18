package com.mapvendor.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

@JsonInclude(JsonInclude.Include.ALWAYS)
public final class ApiResponse<T> {
    private final String code;
    private final String message;
    private final T data;
    private final String requestId;

    private ApiResponse(String code, String message, T data, String requestId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>("OK", "success", data, MDC.get("requestId"));
    }

    public static ApiResponse<Void> failure(String code, String message) {
        return new ApiResponse<Void>(code, message, null, MDC.get("requestId"));
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public String getRequestId() { return requestId; }
}
