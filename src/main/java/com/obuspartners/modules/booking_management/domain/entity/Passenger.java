package com.obuspartners.modules.booking_management.domain.entity;

import com.obuspartners.modules.booking_management.domain.enums.CancellationType;
import com.obuspartners.modules.booking_management.domain.enums.Gender;
import com.obuspartners.modules.booking_management.domain.enums.PassengerCategory;
import com.obuspartners.modules.booking_management.domain.enums.RefundStatus;
import com.obuspartners.modules.booking_management.domain.enums.TicketStatus;
import com.obuspartners.modules.common.domain.entity.BaseEntity;

import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Passenger entity representing an individual passenger in a booking
 * Contains personal details, route information, seat details, and ticket management
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
@Entity
@Table(name = "passengers")
public class Passenger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;

    // === PASSENGER PERSONAL DETAILS ===
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    @NotNull(message = "Gender is required")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_category", nullable = false)
    @NotNull(message = "Passenger category is required")
    private PassengerCategory category;

    @Size(max = 50, message = "Passport number must not exceed 50 characters")
    @Column(name = "passport_number")
    private String passportNumber;

    @Size(max = 50, message = "National ID must not exceed 50 characters")
    @Column(name = "national_id")
    private String nationalId;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone_number")
    private String phoneNumber;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email")
    private String email;

    // === INDIVIDUAL ROUTE INFORMATION ===
    @Size(max = 100, message = "Boarding point must not exceed 100 characters")
    @Column(name = "boarding_point")
    private String boardingPoint; // Specific boarding point for this passenger

    @Size(max = 100, message = "Dropping point must not exceed 100 characters")
    @Column(name = "dropping_point")
    private String droppingPoint; // Specific dropping point for this passenger

    @Column(name = "boarding_time")
    private LocalTime boardingTime;

    @Column(name = "dropping_time")
    private LocalTime droppingTime;

    // === SEAT AND INDIVIDUAL FARE ===
    @NotBlank(message = "Seat ID is required")
    @Size(max = 10, message = "Seat ID must not exceed 10 characters")
    @Column(name = "seat_id", nullable = false)
    private String seatId;

    @NotNull(message = "Individual fare is required")
    @Column(name = "individual_fare", nullable = false)
    private BigDecimal individualFare; // This passenger's fare portion

    // === TICKET INFORMATION ===
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false)
    private TicketStatus ticketStatus = TicketStatus.ACTIVE;

    @Size(max = 100, message = "Ticket number must not exceed 100 characters")
    @Column(name = "ticket_number")
    private String ticketNumber;

    @Column(name = "ticket_issued_at")
    private LocalDateTime ticketIssuedAt;

    @Column(name = "ticket_expires_at")
    private LocalDateTime ticketExpiresAt;

    // === CANCELLATION INFORMATION ===
    @Column(name = "is_cancelled", nullable = false)
    private Boolean isCancelled = false;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_type")
    private CancellationType cancellationType;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus = RefundStatus.NONE;

    @Column(name = "refund_processed_at")
    private LocalDateTime refundProcessedAt;

    @Size(max = 100, message = "Refund reference must not exceed 100 characters")
    @Column(name = "refund_reference")
    private String refundReference;

    // === EXTERNAL SYSTEM REFERENCES ===
    @Size(max = 100, message = "External ticket ID must not exceed 100 characters")
    @Column(name = "external_ticket_id")
    private String externalTicketId;

    @Size(max = 100, message = "External passenger ID must not exceed 100 characters")
    @Column(name = "external_passenger_id")
    private String externalPassengerId;

    // === RELATIONSHIP TO BOOKING ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @NotNull(message = "Booking is required")
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Booking booking;

    @PrePersist
    public void ensureUid() {
        if (uid == null) {
            uid = new ULID().nextULID();
        }
        if (ticketIssuedAt == null) {
            ticketIssuedAt = LocalDateTime.now();
        }
    }
}
