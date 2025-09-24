package com.obuspartners.modules.partner_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for partner statistics
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerStatisticsDto {

    private long totalPartners;
    private long activePartners;
    private long inactivePartners;
    private long verifiedPartners;
    private long unverifiedPartners;
    private long suspendedPartners;
    private long pendingVerificationPartners;
    private long rejectedPartners;
    private long terminatedPartners;
    
    // Breakdown by type
    private Map<String, Long> partnersByType;
    
    // Breakdown by tier
    private Map<String, Long> partnersByTier;
    
    // Breakdown by status
    private Map<String, Long> partnersByStatus;
    
    // Geographic distribution
    private Map<String, Long> partnersByCountry;
    private Map<String, Long> partnersByState;
    private Map<String, Long> partnersByCity;
    
    // Commission statistics
    private double averageCommissionRate;
    private double minCommissionRate;
    private double maxCommissionRate;
    
    // Recent activity
    private long partnersCreatedLast30Days;
    private long partnersUpdatedLast30Days;
    private long partnersVerifiedLast30Days;
}
