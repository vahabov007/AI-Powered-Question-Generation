package com.vahabvahabov.AI_Powered_Question_Generation_Module.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private int status;
    private String timestamp = Instant.now().toString();
    private String path;
    private Map<String, String> errors;
    private String errorCode;

    public static <T> ApiResponse<T> success(String message, T data, int status, String path) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setStatus(status);
        response.setPath(path);
        return response;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return success(message, data, 200, null);
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null, 200, null);
    }

    public static <T> ApiResponse<T> error(String message, int status, String path) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setStatus(status);
        response.setPath(path);
        return response;
    }

    public static <T> ApiResponse<T> error(String message, int status, String path, Map<String, String> errors) {
        ApiResponse<T> response = error(message, status, path);
        response.setErrors(errors);
        return response;
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, int status, String path) {
        ApiResponse<T> response = error(message, status, path);
        response.setErrorCode(errorCode);
        return response;
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, int status, String path, Map<String, String> errors) {
        ApiResponse<T> response = error(message, status, path, errors);
        response.setErrorCode(errorCode);
        return response;
    }
}