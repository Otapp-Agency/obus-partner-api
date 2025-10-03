package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.GroupAgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.GroupAgentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for GroupAgent response data
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupAgentResponseDto {

    private Long id;
    private String uid;
    private String code;
    private String name;
    private String description;
    private String externalSystemIdentifier;
    private GroupAgentType type;
    private GroupAgentStatus status;
    
    // Contact information
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    
    // Business information
    private String businessName;
    private String businessAddress;
    private String taxId;
    private String licenseNumber;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime lastActivityDate;
    private String notes;
    
    // Partner information
    private Long partnerId;
    private String partnerUid;
    private String partnerCode;
    private String partnerBusinessName;
    
    // Statistics
    private int agentCount;
    private int busCoreSystemCount;
    private int activeBusCoreSystemCount;
    
    // Bus core systems (summary)
    private java.util.List<GroupAgentCoreBusSystemSummaryDto> busCoreSystems;
}
