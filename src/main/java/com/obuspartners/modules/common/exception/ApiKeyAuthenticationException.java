package com.obuspartners.modules.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for API key authentication failures
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class ApiKeyAuthenticationException extends ApiException {
    
    private final String errorType;
    
    public ApiKeyAuthenticationException(String message, String errorType) {
        super(message, HttpStatus.UNAUTHORIZED);
        this.errorType = errorType;
    }
    
    public ApiKeyAuthenticationException(String message, String errorType, HttpStatus status) {
        super(message, status);
        this.errorType = errorType;
    }
    
    public String getErrorType() {
        return errorType;
    }
}
