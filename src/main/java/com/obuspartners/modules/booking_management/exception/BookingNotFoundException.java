package com.obuspartners.modules.booking_management.exception;

/**
 * Exception thrown when booking is not found
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class BookingNotFoundException extends RuntimeException {
    
    public BookingNotFoundException(String message) {
        super(message);
    }
    
    public BookingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
