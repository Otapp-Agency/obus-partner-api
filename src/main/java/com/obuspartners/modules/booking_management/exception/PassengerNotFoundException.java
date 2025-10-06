package com.obuspartners.modules.booking_management.exception;

/**
 * Exception thrown when passenger is not found
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class PassengerNotFoundException extends RuntimeException {
    
    public PassengerNotFoundException(String message) {
        super(message);
    }
    
    public PassengerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
