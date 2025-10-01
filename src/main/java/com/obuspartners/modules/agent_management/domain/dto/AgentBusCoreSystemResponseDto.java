package com.obuspartners.modules.agent_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for AgentBusCoreSystem response
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentBusCoreSystemResponseDto {

    private Long id;
    private String uid;
    private Long agentId;
    private String agentName;
    private String agentContactPerson;
    private String agentBusinessName;
    private String agentPhoneNumber;
    private String agentEmail;
    
    private Long busCoreSystemId;
    private String busCoreSystemName;
    private String busCoreSystemCode;
    
    // Authentication credentials (masked for security)
    private String agentLoginName;
    private String password; // Only include in specific contexts
    private String txnUserName;
    private String txnPassword; // Only include in specific contexts
    
    // Status
    private String agentStatusInBusCore;
    private Boolean isActive;
    private Boolean isPrimary;
    private String busCoreAgentId;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime lastAuthenticationDate;
    private LocalDateTime lastBookingDate;
    
    // Helper methods
    public boolean isActiveInBusCore() {
        return isActive && "ACTIVE".equalsIgnoreCase(agentStatusInBusCore);
    }
    
    public boolean hasValidCredentials() {
        return agentLoginName != null && !agentLoginName.trim().isEmpty() &&
               password != null && !password.trim().isEmpty();
    }
}
