package com.obuspartners.modules.common.exception;

/**
 * Exception thrown when trying to create a resource that already exists
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
