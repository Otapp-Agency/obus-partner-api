package com.obuspartners.modules.booking_management.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Booking Payment Event for initiating payment processing
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPaymentEvent {
    
    private String eventId;
    @Builder.Default
    private String eventType = "BOOKING_PAYMENT_INITIATED";
    
    // Booking Information
    private String bookingUid;
    private Long bookingId;
    
    // Payment Information
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentProvider; // MIXX, BMSLG, etc.
    
    // Partner Information
    private Long partnerId;
    private String partnerCode;
    private Long agentId;
    private String agentCode;
    
    // Customer Information
    private String customerPhone;
    private String customerEmail;
    private String customerName;
    
    // External References
    private String externalBookingId;
    private String externalReference;
    
    // Payment Provider Specific Data
    private String paymentProviderReference;
    private String callbackUrl;
    private String returnUrl;
    
    // Metadata
    private String description;
    private String notes;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    // Additional context
    private java.util.Map<String, Object> metadata;
}
