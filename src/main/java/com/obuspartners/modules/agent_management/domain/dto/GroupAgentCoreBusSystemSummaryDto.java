package com.obuspartners.modules.agent_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for GroupAgentCoreBusSystem summary data (used in lists)
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupAgentCoreBusSystemSummaryDto {

    private Long id;
    private String uid;
    
    // Basic information
    private String externalAgentIdentifier;
    private String username;
    private Boolean isActive;
    private Boolean isPrimary;
    private String externalSystemStatus;
    
    // Bus core system information
    private Long busCoreSystemId;
    private String busCoreSystemCode;
    private String busCoreSystemName;
    private String busCoreSystemProviderName;
    
    // Timestamps
    private LocalDateTime lastAuthenticationDate;
    private LocalDateTime lastSyncDate;
}
