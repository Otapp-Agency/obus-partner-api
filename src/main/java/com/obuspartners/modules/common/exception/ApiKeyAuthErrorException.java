package com.obuspartners.modules.common.exception;

/**
 * Exception for API key authentication errors (system errors, etc.)
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class ApiKeyAuthErrorException extends ApiKeyAuthenticationException {
    
    public ApiKeyAuthErrorException(String message) {
        super("API key authentication failed: " + message, "AUTH_ERROR");
    }
    
    public ApiKeyAuthErrorException(String message, Throwable cause) {
        super("API key authentication failed: " + message, "AUTH_ERROR");
        initCause(cause);
    }
}
