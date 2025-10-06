package com.obuspartners.modules.booking_management.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Callback Event for handling payment provider responses
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackEvent {
    
    private String eventId;
    @Builder.Default
    private String eventType = "PAYMENT_CALLBACK_RECEIVED";
    
    // Booking Information
    private String bookingUid;
    private Long bookingId;
    
    // Payment Information
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentProvider; // MIXX, BMSLG, etc.
    
    // Payment Status
    private String paymentStatus; // SUCCESS, FAILED, PENDING, CANCELLED
    private String transactionId;
    private String paymentProviderReference;
    private String externalTransactionId;
    
    // Callback Information
    private String callbackStatus;
    private String callbackMessage;
    private String callbackCode;
    private String callbackReference;
    
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
    
    // Metadata
    private String description;
    private String notes;
    private String failureReason;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    // Additional context
    private java.util.Map<String, Object> metadata;
}
