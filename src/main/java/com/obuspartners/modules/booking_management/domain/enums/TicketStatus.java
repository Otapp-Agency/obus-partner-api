package com.obuspartners.modules.booking_management.domain.enums;

/**
 * Ticket status enumeration
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum TicketStatus {
    ACTIVE,         // Ticket is valid and active
    CANCELLED,      // Ticket has been cancelled
    EXPIRED,        // Ticket has expired
    USED,          // Ticket has been used for travel
    REFUNDED        // Ticket has been refunded
}
