package com.obuspartners.modules.partner_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.obuspartners.modules.partner_management.domain.enums.PartnerStatus;
import com.obuspartners.modules.partner_management.domain.enums.PartnerTier;
import com.obuspartners.modules.partner_management.domain.enums.PartnerType;

/**
 * DTO for partner response data
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerResponseDto {

    private Long id;
    private String uid;
    private String code;
    private String businessName;
    private String legalName;
    private String email;
    private String phoneNumber;
    private String businessRegistrationNumber;
    private String taxIdentificationNumber;
    private String businessAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private PartnerStatus status;
    private PartnerType type;
    private PartnerTier tier;
    private Boolean isActive;
    private Boolean isVerified;
    private Double commissionRate;
    private String contactPersonName;
    private String contactPersonEmail;
    private String contactPersonPhone;
    private String description;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Audit information
    private String createdByUsername;
    private String createdByEmail;
    private String updatedByUsername;
    private String updatedByEmail;
}
