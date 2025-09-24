package com.obuspartners.modules.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom API exception for handling application-specific errors
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatusCode() {
        return status;
    }
}
