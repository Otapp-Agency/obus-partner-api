package com.obus.partner.modules.common.exception;

/**
 * Exception thrown when trying to create a user that already exists
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
