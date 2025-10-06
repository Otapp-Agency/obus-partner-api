package com.obuspartners.modules.booking_management.domain.enums;

/**
 * Booking status enumeration
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum BookingStatus {
    PROCESSING,    // Initial state when booking is created
    CONFIRMED,     // Booking confirmed by external system
    CANCELLED,      // Booking cancelled
    FAILED,         // Booking failed
    REFUNDED        // Booking refunded
}
