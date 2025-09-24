package com.obuspartners.modules.partner_management.domain.dto;

import com.obuspartners.modules.partner_management.domain.enums.PartnerStatus;
import com.obuspartners.modules.partner_management.domain.enums.PartnerTier;
import com.obuspartners.modules.partner_management.domain.enums.PartnerType;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing partner
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePartnerRequestDto {

    @Size(max = 255, message = "Business name must not exceed 255 characters")
    private String businessName;

    @Size(max = 255, message = "Legal name must not exceed 255 characters")
    private String legalName;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,20}$", message = "Phone number format is invalid")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Size(max = 100, message = "Business registration number must not exceed 100 characters")
    private String businessRegistrationNumber;

    @Size(max = 100, message = "Tax identification number must not exceed 100 characters")
    private String taxIdentificationNumber;

    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    private PartnerStatus status;

    private PartnerType type;

    private PartnerTier tier;

    private Boolean isActive;

    private Boolean isVerified;

    @Size(max = 255, message = "Contact person name must not exceed 255 characters")
    private String contactPersonName;

    @Email(message = "Contact person email must be valid")
    @Size(max = 255, message = "Contact person email must not exceed 255 characters")
    private String contactPersonEmail;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,20}$", message = "Contact person phone format is invalid")
    @Size(max = 20, message = "Contact person phone must not exceed 20 characters")
    private String contactPersonPhone;

    @Min(value = 0, message = "Commission rate must be non-negative")
    @Max(value = 100, message = "Commission rate must not exceed 100")
    private Double commissionRate;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
