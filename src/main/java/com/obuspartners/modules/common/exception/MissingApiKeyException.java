package com.obuspartners.modules.common.exception;

/**
 * Exception for missing API key or secret
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class MissingApiKeyException extends ApiKeyAuthenticationException {
    
    public MissingApiKeyException() {
        super("API key and secret are required", "MISSING_CREDENTIALS");
    }
    
    public MissingApiKeyException(String message) {
        super(message, "MISSING_CREDENTIALS");
    }
}
