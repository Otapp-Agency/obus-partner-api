package com.obuspartners.modules.booking_management.domain.enums;

/**
 * Refund status enumeration
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum RefundStatus {
    NONE,           // No refund requested
    PENDING,        // Refund requested, processing
    PROCESSED,      // Refund processed
    FAILED,         // Refund failed
    PARTIAL         // Partial refund processed
}
