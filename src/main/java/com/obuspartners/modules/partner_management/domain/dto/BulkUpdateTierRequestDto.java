package com.obuspartners.modules.partner_management.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * DTO for bulk update partner tier request
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUpdateTierRequestDto {
    
    @NotEmpty(message = "Partner IDs list cannot be empty")
    private List<Long> partnerIds;
    
    @NotNull(message = "Tier is required")
    private com.obuspartners.modules.partner_management.domain.enums.PartnerTier tier;
}
