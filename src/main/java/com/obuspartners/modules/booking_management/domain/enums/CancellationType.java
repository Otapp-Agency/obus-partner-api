package com.obuspartners.modules.booking_management.domain.enums;

/**
 * Cancellation type enumeration
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum CancellationType {
    VOLUNTARY,      // Passenger cancelled voluntarily
    INVOLUNTARY,    // Cancelled by system/operator
    NO_SHOW,        // Passenger didn't show up
    OPERATOR_CANCELLED // Bus/route cancelled by operator
}
