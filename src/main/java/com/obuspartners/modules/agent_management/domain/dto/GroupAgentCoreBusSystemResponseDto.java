package com.obuspartners.modules.agent_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for GroupAgentCoreBusSystem response data
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupAgentCoreBusSystemResponseDto {

    private Long id;
    private String uid;
    
    // External system identification
    private String externalAgentIdentifier;
    private String username;
    
    // Configuration and status
    private Boolean isActive;
    private Boolean isPrimary;
    private String externalSystemStatus;
    private String externalAgentId;
    
    // Configuration
    private String configuration;
    private String endpointUrl;
    private Integer timeoutSeconds;
    private Integer retryAttempts;
    
    // Audit Fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime lastAuthenticationDate;
    private LocalDateTime lastSyncDate;
    private String notes;
    
    // Bus core system information
    private Long busCoreSystemId;
    private String busCoreSystemUid;
    private String busCoreSystemCode;
    private String busCoreSystemName;
    private String busCoreSystemProviderName;
    private String busCoreSystemBaseUrl;
    
    // Group agent information
    private Long groupAgentId;
    private String groupAgentUid;
    private String groupAgentCode;
    private String groupAgentName;
    
    // Partner information
    private Long partnerId;
    private String partnerUid;
    private String partnerCode;
    private String partnerBusinessName;
}
