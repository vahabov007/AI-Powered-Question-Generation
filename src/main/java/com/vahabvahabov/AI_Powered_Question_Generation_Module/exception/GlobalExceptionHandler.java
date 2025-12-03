package com.vahabvahabov.AI_Powered_Question_Generation_Module.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.customExceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Custom-Exception
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException ex,
                                                                HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getHttpStatus().value(),
                request.getRequestURI()
        );

        log.error("Custom exception occurred: {}", ex.getMessage());
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // QuestionGeneration-Exception
    @ExceptionHandler(QuestionGenerationException.class)
    public ResponseEntity<ApiResponse<?>> handleQuestionGenerationException(QuestionGenerationException ex,
                                                                            HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getHttpStatus().value(),
                request.getRequestURI()
        );

        log.warn("Question generation failed: {}", ex.getMessage());
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // AIService-Exception
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleAIServiceException(AIServiceException ex,
                                                                   HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                "AI service is temporarily unavailable. Please try again later.",
                ex.getErrorCode(),
                ex.getHttpStatus().value(),
                request.getRequestURI()
        );

        log.error("AI service error: {}", ex.getMessage());
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // ResourceNotFound-Exception
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                          HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getHttpStatus().value(),
                request.getRequestURI()
        );

        log.warn("Resource not found: {}", ex.getMessage());
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // RateLimit-Exception
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<?>> handleRateLimitException(RateLimitException ex,
                                                                   HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getHttpStatus().value(),
                request.getRequestURI()
        );

        log.warn("Rate limit exceeded: {}", ex.getMessage());
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // Authentification-Exception
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException ex,
                                                                        HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getHttpStatus().value(),
                request.getRequestURI()
        );

        log.warn("Authentication failed: {}", ex.getMessage());
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // Validation-Exception
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(ValidationException ex,
                                                                    HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getHttpStatus().value(),
                request.getRequestURI()
        );

        log.warn("Validation error: {}", ex.getMessage());
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // MethodArgumentNotValid-Exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                     HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<?> response = ApiResponse.error(
                "Validation failed",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                errors
        );

        log.warn("Validation errors: {}", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // DataIntegrityViolation-Exception
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                       HttpServletRequest request) {
        String message = "Data integrity violation";

        // Check for specific constraint violations
        if (ex.getMessage() != null) {
            if (ex.getMessage().toLowerCase().contains("unique") ||
                    ex.getMessage().toLowerCase().contains("duplicate")) {
                message = "Resource already exists";
            } else if (ex.getMessage().toLowerCase().contains("foreign key")) {
                message = "Cannot perform operation due to reference constraint";
            }
        }

        ApiResponse<?> response = ApiResponse.error(
                message,
                "DATA_INTEGRITY_VIOLATION",
                HttpStatus.CONFLICT.value(),
                request.getRequestURI()
        );

        log.error("Data integrity violation: {}", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // AccessDenied-Exception
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex,
                                                                      HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                "You do not have permission to access this resource",
                "ACCESS_DENIED",
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI()
        );

        log.warn("Access denied for request: {}", request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // ResponseStatus-Exception
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleResponseStatusException(ResponseStatusException ex,
                                                                        HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                ex.getReason(),
                ex.getStatusCode().toString(),
                ex.getStatusCode().value(),
                request.getRequestURI()
        );

        log.warn("Response status exception: {}", ex.getReason());
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    // IllegalArgument-Exception
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                         HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                ex.getMessage(),
                "ILLEGAL_ARGUMENT",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );

        log.warn("Illegal argument: {}", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Runtime-Exception
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex,
                                                                 HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                "An unexpected error occurred",
                "RUNTIME_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );

        log.error("Runtime exception: ", ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Exception (All)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllUncaughtException(Exception ex,
                                                                     HttpServletRequest request) {
        ApiResponse<?> response = ApiResponse.error(
                "An unexpected server error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );

        log.error("Unhandled exception: ", ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}