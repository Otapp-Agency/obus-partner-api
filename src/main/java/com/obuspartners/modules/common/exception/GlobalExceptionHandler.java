package com.obuspartners.modules.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.ServletException;

import com.obuspartners.modules.common.util.ResponseWrapper;

/**
 * Global exception handler for consistent error responses
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseWrapper<>(false, 404, ex.getMessage(), null));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        log.error("Duplicate resource: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ResponseWrapper<>(false, 409, ex.getMessage(), null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.error("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, 401, "Authentication failed", null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, 401, "Invalid username or password", null));
    }

    @ExceptionHandler(ApiKeyAuthenticationException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleApiKeyAuthenticationException(
            ApiKeyAuthenticationException ex, WebRequest request) {
        log.error("API Key Authentication Exception: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ResponseWrapper<>(false, ex.getStatusCode().value(), ex.getMessage(), null));
    }

    @ExceptionHandler(MissingApiKeyException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleMissingApiKeyException(
            MissingApiKeyException ex, WebRequest request) {
        log.warn("Missing API key: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, 401, ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleInvalidApiKeyException(
            InvalidApiKeyException ex, WebRequest request) {
        log.warn("Invalid API key: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, 401, ex.getMessage(), null));
    }

    @ExceptionHandler(ApiKeyAuthErrorException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleApiKeyAuthErrorException(
            ApiKeyAuthErrorException ex, WebRequest request) {
        log.error("API Key Auth Error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, 401, ex.getMessage(), null));
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleServletException(
            ServletException ex, WebRequest request) {
        // Check if the ServletException wraps an ApiKeyAuthenticationException
        Throwable cause = ex.getCause();
        if (cause instanceof ApiKeyAuthenticationException) {
            ApiKeyAuthenticationException apiKeyEx = (ApiKeyAuthenticationException) cause;
            log.error("API Key Authentication Exception (via ServletException): {}", apiKeyEx.getMessage());
            return ResponseEntity.status(apiKeyEx.getStatusCode())
                    .body(new ResponseWrapper<>(false, apiKeyEx.getStatusCode().value(), apiKeyEx.getMessage(), null));
        }
        
        // Handle other ServletExceptions
        log.error("Servlet Exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseWrapper<>(false, 500, "Internal server error", null));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleApiException(
            ApiException ex, WebRequest request) {
        log.error("API Exception: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ResponseWrapper<>(false, ex.getStatusCode().value(), ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        // Log only a simple message without detailed field errors
        log.warn("Validation failed for request: {}", request.getDescription(false));
        
        // Collect all validation errors into a single message string
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errorMessage.append(fieldName).append(" ").append(message).append(", ");
        });
        
        // Remove the last comma and space
        String finalMessage = errorMessage.toString().replaceAll(", $", "");
        
        // Return validation errors as a single message
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseWrapper<>(false, 400, finalMessage, null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseWrapper<>(false, 400, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseWrapper<>(false, 500, "An unexpected error occurred", null));
    }
}
