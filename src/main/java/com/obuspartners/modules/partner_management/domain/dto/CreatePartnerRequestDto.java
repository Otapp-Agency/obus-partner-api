package com.obuspartners.modules.partner_management.domain.dto;

import com.obuspartners.modules.partner_management.domain.enums.PartnerType;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new partner
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePartnerRequestDto {

    @NotBlank(message = "Business name is required")
    @Size(max = 255, message = "Business name must not exceed 255 characters")
    private String businessName;

    @NotBlank(message = "Legal name is required")
    @Size(max = 255, message = "Legal name must not exceed 255 characters")
    private String legalName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,20}$", message = "Phone number format is invalid")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Business registration number is required")
    @Size(max = 100, message = "Business registration number must not exceed 100 characters")
    private String businessRegistrationNumber;

    @NotBlank(message = "Tax identification number is required")
    @Size(max = 100, message = "Tax identification number must not exceed 100 characters")
    private String taxIdentificationNumber;

    @NotBlank(message = "Business address is required")
    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @NotNull(message = "Partner type is required")
    private PartnerType type;

    @NotBlank(message = "Contact person name is required")
    @Size(max = 255, message = "Contact person name must not exceed 255 characters")
    private String contactPersonName;

    @NotBlank(message = "Contact person email is required")
    @Email(message = "Contact person email must be valid")
    @Size(max = 255, message = "Contact person email must not exceed 255 characters")
    private String contactPersonEmail;

    @NotBlank(message = "Contact person phone is required")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,20}$", message = "Contact person phone format is invalid")
    @Size(max = 20, message = "Contact person phone must not exceed 20 characters")
    private String contactPersonPhone;

    @Min(value = 0, message = "Commission rate must be non-negative")
    @Max(value = 100, message = "Commission rate must not exceed 100")
    @Builder.Default
    private Double commissionRate = 0.0;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
