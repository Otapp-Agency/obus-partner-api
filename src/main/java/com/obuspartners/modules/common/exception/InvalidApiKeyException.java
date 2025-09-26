package com.obuspartners.modules.common.exception;

/**
 * Exception for invalid API key or secret
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class InvalidApiKeyException extends ApiKeyAuthenticationException {
    
    public InvalidApiKeyException() {
        super("Invalid API key or secret provided", "INVALID_CREDENTIALS");
    }
    
    public InvalidApiKeyException(String message) {
        super(message, "INVALID_CREDENTIALS");
    }
    
    public InvalidApiKeyException(String apiKey, String reason) {
        super("Invalid API key or secret provided: " + reason, "INVALID_CREDENTIALS");
    }
}
