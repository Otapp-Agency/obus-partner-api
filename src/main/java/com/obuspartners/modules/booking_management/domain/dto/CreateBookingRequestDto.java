package com.obuspartners.modules.booking_management.domain.dto;

import com.obuspartners.modules.booking_management.domain.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO for creating a new booking request
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new booking")
public class CreateBookingRequestDto {

    @NotNull(message = "Agent ID is required")
    @Schema(description = "ID of the agent making the booking", example = "1")
    private Long agentId;

    @NotNull(message = "Bus core system ID is required")
    @Schema(description = "ID of the bus core system", example = "1")
    private Long busCoreSystemId;

    // === COMPANY INFORMATION ===
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    @Schema(description = "Name of the bus company", example = "Dar Express")
    private String companyName;

    @Size(max = 100, message = "Company code must not exceed 100 characters")
    @Schema(description = "Company code", example = "DAREXP")
    private String companyCode;

    @Size(max = 100, message = "Company registration number must not exceed 100 characters")
    @Schema(description = "Company registration number", example = "REG123456")
    private String companyRegistrationNumber;

    // === BUS INFORMATION ===
    @NotBlank(message = "Bus number is required")
    @Size(max = 100, message = "Bus number must not exceed 100 characters")
    @Schema(description = "Bus number", example = "BUS001")
    private String busNumber;

    @Size(max = 100, message = "Bus type must not exceed 100 characters")
    @Schema(description = "Type of bus", example = "EXPRESS")
    private String busType;

    @Size(max = 100, message = "Bus model must not exceed 100 characters")
    @Schema(description = "Bus model", example = "Mercedes Benz")
    private String busModel;

    @Size(max = 50, message = "Bus plate number must not exceed 50 characters")
    @Schema(description = "Bus plate number", example = "T123ABC")
    private String busPlateNumber;

    @Schema(description = "Bus capacity", example = "50")
    private Integer busCapacity;

    // === ROUTE INFORMATION ===
    @NotBlank(message = "Route name is required")
    @Size(max = 100, message = "Route name must not exceed 100 characters")
    @Schema(description = "Route name", example = "Dar es Salaam - Arusha")
    private String routeName;

    @NotBlank(message = "Departure station is required")
    @Size(max = 100, message = "Departure station must not exceed 100 characters")
    @Schema(description = "Departure station", example = "Dar es Salaam")
    private String departureStation;

    @NotBlank(message = "Arrival station is required")
    @Size(max = 100, message = "Arrival station must not exceed 100 characters")
    @Schema(description = "Arrival station", example = "Arusha")
    private String arrivalStation;

    @NotNull(message = "Departure date is required")
    @Schema(description = "Departure date", example = "2024-01-15")
    private LocalDate departureDate;

    @NotNull(message = "Departure time is required")
    @Schema(description = "Departure time", example = "08:00")
    private LocalTime departureTime;

    @Schema(description = "Arrival time", example = "14:00")
    private LocalTime arrivalTime;

    @Schema(description = "Estimated duration in minutes", example = "360")
    private Integer estimatedDurationMinutes;

    // === FARE INFORMATION ===
    @NotNull(message = "Base fare is required")
    @Schema(description = "Base fare amount", example = "50000.00")
    private BigDecimal baseFare;

    @Schema(description = "Tax amount", example = "5000.00")
    private BigDecimal taxAmount;

    @Schema(description = "Service charge", example = "2000.00")
    private BigDecimal serviceCharge;

    @Schema(description = "Discount amount", example = "5000.00")
    private BigDecimal discountAmount;

    @NotBlank(message = "Currency is required")
    @Size(max = 3, message = "Currency must not exceed 3 characters")
    @Schema(description = "Currency code", example = "TZS")
    private String currency = "TZS";

    @NotNull(message = "Payment method is required")
    @Schema(description = "Payment method", example = "CASH")
    private PaymentMethod paymentMethod;

    // === PASSENGERS ===
    @Valid
    @NotEmpty(message = "At least one passenger is required")
    @Schema(description = "List of passengers")
    private List<PassengerDto> passengers;

    // === METADATA ===
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Special requirements")
    private String notes;

    @Size(max = 50, message = "Booking source must not exceed 50 characters")
    @Schema(description = "Source of booking", example = "API")
    private String bookingSource;

    @Size(max = 50, message = "Promo code must not exceed 50 characters")
    @Schema(description = "Promotional code", example = "SAVE10")
    private String promoCode;
}
