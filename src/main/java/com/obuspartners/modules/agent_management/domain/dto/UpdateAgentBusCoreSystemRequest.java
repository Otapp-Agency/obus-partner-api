package com.obuspartners.modules.agent_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating agent bus core system configuration
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgentBusCoreSystemRequest {

    private String agentLoginName;
    private String password; // Plain text password - will be encrypted by service
    private String txnUserName;
    private String txnPassword; // Plain text transaction password - will be encrypted by service
    private Boolean isActive;
    private Boolean isPrimary;
    private String agentStatusInBusCore;
    private String busCoreAgentId;
}
