package com.obuspartners.modules.booking_management.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgBookSeatRequestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Booking Created Event for Kafka-based booking processing
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    
    private String eventId;
    @Builder.Default
    private String eventType = "BOOKING_CREATED";

    // BMSLG Data
    private BmsLgBookSeatRequestDto bmsLgBookingRequest;
    
    // Booking Details
    private String bookingUid;
    private String status;
    private BigDecimal totalBookingFare;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
    
    // Company Information
    private String companyName;
    private String companyCode;
    private String companyRegistrationNumber;
    
    // Bus Information
    private String busNumber;
    private String busType;
    private String busModel;
    private String busPlateNumber;
    private Integer busCapacity;
    
    // Route Information
    private String routeName;
    private String departureStation;
    private String arrivalStation;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate departureDate;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime departureTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime arrivalTime;
    
    private Integer estimatedDurationMinutes;
    
    // Relationships
    private Long partnerId;
    private String partnerUid;
    private String partnerCode;
    private Long agentId;
    private String agentUid;
    private String agentCode;
    private Long busCoreSystemId;
    private String busCoreSystemUid;
    private String busCoreSystemCode;
    
    // External System References
    private String externalBookingId;
    private String externalRouteId;
    private String externalBusId;
    private String externalReference;
    
    // Passenger Information
    private List<PassengerEventData> passengers;
    
    // Metadata
    private String notes;
    private String bookingSource;
    private String promoCode;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    // Additional context
    private Map<String, Object> metadata;
    
    /**
     * Passenger data for the event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerEventData {
        private String passengerUid;
        private String fullName;
        private String gender;
        private String category;
        private String seatId;
        private BigDecimal individualFare;
        private String boardingPoint;
        private String droppingPoint;
        
        @JsonFormat(pattern = "HH:mm")
        private LocalTime boardingTime;
        
        @JsonFormat(pattern = "HH:mm")
        private LocalTime droppingTime;
        
        private String phoneNumber;
        private String email;
        private String passportNumber;
        private String nationalId;
        private String externalTicketId;
        private String externalPassengerId;
    }
    
    /**
     * Factory method to create booking created event
     */
    public static BookingCreatedEvent create(String bookingUid, String eventId) {
        return BookingCreatedEvent.builder()
                .eventId(eventId)
                .bookingUid(bookingUid)
                .eventType("BOOKING_CREATED")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
