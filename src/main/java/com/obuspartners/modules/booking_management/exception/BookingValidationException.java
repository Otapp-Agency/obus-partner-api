package com.obuspartners.modules.booking_management.exception;

/**
 * Exception thrown when booking validation fails
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class BookingValidationException extends RuntimeException {
    
    public BookingValidationException(String message) {
        super(message);
    }
    
    public BookingValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
