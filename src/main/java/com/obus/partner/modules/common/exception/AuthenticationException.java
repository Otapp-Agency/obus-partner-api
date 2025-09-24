package com.obus.partner.modules.common.exception;

/**
 * Custom authentication exception
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
