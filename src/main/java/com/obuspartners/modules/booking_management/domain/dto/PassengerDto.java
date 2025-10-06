package com.obuspartners.modules.booking_management.domain.dto;

import com.obuspartners.modules.booking_management.domain.enums.Gender;
import com.obuspartners.modules.booking_management.domain.enums.PassengerCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * DTO for passenger information in booking request
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Passenger information for booking")
public class PassengerDto {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Schema(description = "Passenger full name", example = "John Doe")
    private String fullName;

    @NotNull(message = "Gender is required")
    @Schema(description = "Passenger gender", example = "MALE")
    private Gender gender;

    @NotNull(message = "Passenger category is required")
    @Schema(description = "Passenger category", example = "ADULT")
    private PassengerCategory category;

    // Individual boarding/dropping points
    @Size(max = 100, message = "Boarding point must not exceed 100 characters")
    @Schema(description = "Boarding point for this passenger", example = "Ubungo Terminal")
    private String boardingPoint;

    @Size(max = 100, message = "Dropping point must not exceed 100 characters")
    @Schema(description = "Dropping point for this passenger", example = "Arusha Terminal")
    private String droppingPoint;

    @Schema(description = "Boarding time", example = "08:30")
    private LocalTime boardingTime;

    @Schema(description = "Dropping time", example = "13:30")
    private LocalTime droppingTime;

    // Seat and fare
    @NotBlank(message = "Seat ID is required")
    @Size(max = 10, message = "Seat ID must not exceed 10 characters")
    @Schema(description = "Seat ID", example = "A1")
    private String seatId;

    @NotNull(message = "Individual fare is required")
    @Schema(description = "Individual fare for this passenger", example = "25000.00")
    private BigDecimal individualFare;

    // Contact information
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Schema(description = "Phone number", example = "255700000000")
    private String phoneNumber;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Size(max = 50, message = "Passport number must not exceed 50 characters")
    @Schema(description = "Passport number", example = "A1234567")
    private String passportNumber;

    @Size(max = 50, message = "National ID must not exceed 50 characters")
    @Schema(description = "National ID", example = "123456789")
    private String nationalId;
}
