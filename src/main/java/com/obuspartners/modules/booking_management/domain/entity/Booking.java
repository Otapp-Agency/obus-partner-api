package com.obuspartners.modules.booking_management.domain.entity;

import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.booking_management.domain.enums.BookingStatus;
import com.obuspartners.modules.booking_management.domain.enums.PaymentMethod;
import com.obuspartners.modules.booking_management.domain.enums.PaymentStatus;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import com.obuspartners.modules.common.domain.entity.BaseEntity;
import com.obuspartners.modules.partner_management.domain.entity.Partner;

import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Booking entity representing a bus booking transaction
 * Contains company information, bus details, route information, and fare details
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
@Table(name = "bookings")
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;

    // === RELATIONSHIPS ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    @NotNull(message = "Partner is required")
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    @NotNull(message = "Agent is required")
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Agent agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_core_system_id", nullable = false)
    @NotNull(message = "Bus core system is required")
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BusCoreSystem busCoreSystem;

    // === COMPANY INFORMATION ===
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Size(max = 100, message = "Company code must not exceed 100 characters")
    @Column(name = "company_code")
    private String companyCode;

    @Size(max = 100, message = "Company registration number must not exceed 100 characters")
    @Column(name = "company_registration_number")
    private String companyRegistrationNumber;

    // === BUS INFORMATION ===
    @NotBlank(message = "Bus number is required")
    @Size(max = 100, message = "Bus number must not exceed 100 characters")
    @Column(name = "bus_number", nullable = false)
    private String busNumber;

    @Size(max = 100, message = "Bus type must not exceed 100 characters")
    @Column(name = "bus_type")
    private String busType; // EXPRESS, LUXURY, STANDARD

    @Size(max = 100, message = "Bus model must not exceed 100 characters")
    @Column(name = "bus_model")
    private String busModel;

    @Size(max = 50, message = "Bus plate number must not exceed 50 characters")
    @Column(name = "bus_plate_number")
    private String busPlateNumber;

    @Column(name = "bus_capacity")
    private Integer busCapacity;

    // === ROUTE INFORMATION (Booking Level) ===
    @NotBlank(message = "Route name is required")
    @Size(max = 100, message = "Route name must not exceed 100 characters")
    @Column(name = "route_name", nullable = false)
    private String routeName; // e.g., "Dar es Salaam - Arusha"

    @NotBlank(message = "Departure station is required")
    @Size(max = 100, message = "Departure station must not exceed 100 characters")
    @Column(name = "departure_station", nullable = false)
    private String departureStation;

    @NotBlank(message = "Arrival station is required")
    @Size(max = 100, message = "Arrival station must not exceed 100 characters")
    @Column(name = "arrival_station", nullable = false)
    private String arrivalStation;

    @NotNull(message = "Departure date is required")
    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @NotNull(message = "Departure time is required")
    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    // === FARE AND PAYMENT INFORMATION ===
    @NotNull(message = "Total booking fare is required")
    @Column(name = "total_booking_fare", nullable = false)
    private BigDecimal totalBookingFare;

    @NotNull(message = "Base fare is required")
    @Column(name = "base_fare", nullable = false)
    private BigDecimal baseFare;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "service_charge")
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotBlank(message = "Currency is required")
    @Size(max = 3, message = "Currency must not exceed 3 characters")
    @Column(name = "currency", nullable = false)
    private String currency = "TZS";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // === BOOKING STATUS ===
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.PROCESSING;

    // === EXTERNAL SYSTEM REFERENCES ===
    @Size(max = 100, message = "External booking ID must not exceed 100 characters")
    @Column(name = "external_booking_id")
    private String externalBookingId; // BMSLG booking ID

    @Size(max = 100, message = "External route ID must not exceed 100 characters")
    @Column(name = "external_route_id")
    private String externalRouteId;

    @Size(max = 100, message = "External bus ID must not exceed 100 characters")
    @Column(name = "external_bus_id")
    private String externalBusId;

    @Size(max = 100, message = "External reference must not exceed 100 characters")
    @Column(name = "external_reference")
    private String externalReference;

    // === RELATIONSHIP TO PASSENGERS ===
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Passenger> passengers = new ArrayList<>();

    // === BOOKING METADATA ===
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Size(max = 50, message = "Booking source must not exceed 50 characters")
    @Column(name = "booking_source")
    private String bookingSource; // WEB, MOBILE, AGENT, API

    @Size(max = 50, message = "Promo code must not exceed 50 characters")
    @Column(name = "promo_code")
    private String promoCode;

    @PrePersist
    public void ensureUid() {
        if (uid == null) {
            uid = new ULID().nextULID();
        }
    }
}
