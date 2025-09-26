package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for agent summary data (used in lists and dropdowns)
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSummaryDto {

    private Long id;
    private String uid;
    private String code;
    private String partnerAgentNumber;
    private String loginUsername;
    private String businessName;
    private String contactPerson;
    private AgentType agentType;
    private AgentStatus status;
    private LocalDateTime registrationDate;
    
    // Partner information
    private Long partnerId;
    private String partnerCode;
    private String partnerBusinessName;
    
    // Super agent information (for sub-agents)
    private Long superAgentId;
    private String superAgentCode;
    private String superAgentBusinessName;
}
