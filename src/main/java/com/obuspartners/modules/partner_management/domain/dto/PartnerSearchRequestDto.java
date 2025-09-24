package com.obuspartners.modules.partner_management.domain.dto;

import com.obuspartners.modules.partner_management.domain.enums.PartnerStatus;
import com.obuspartners.modules.partner_management.domain.enums.PartnerTier;
import com.obuspartners.modules.partner_management.domain.enums.PartnerType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for partner search request with multiple criteria
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerSearchRequestDto {

    private String businessName;
    private String legalName;
    private String email;
    private String phoneNumber;
    private String city;
    private String state;
    private String country;
    private PartnerStatus status;
    private PartnerType type;
    private PartnerTier tier;
    private Boolean isActive;
    private Boolean isVerified;
    private Double minCommissionRate;
    private Double maxCommissionRate;
    private String contactPersonName;
    
    // Pagination
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
    
    // Sorting
    private String sortBy;
    private String sortDirection; // ASC, DESC
}
