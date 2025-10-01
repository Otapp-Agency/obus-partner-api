package com.obuspartners.modules.agent_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for decrypted agent credentials
 * Used when credentials need to be provided to external systems (bus core systems)
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecryptedAgentCredentials {
    
    /**
     * Agent login name for the bus core system
     */
    private String agentLoginName;
    
    /**
     * Decrypted password for the bus core system
     */
    private String password;
    
    /**
     * Transaction user name (if applicable)
     */
    private String txnUserName;
    
    /**
     * Decrypted transaction password (if applicable)
     */
    private String txnPassword;
    
    /**
     * Agent status in the bus core system
     */
    private String agentStatusInBusCore;
    
    /**
     * Agent ID in the bus core system
     */
    private String busCoreAgentId;
    
    /**
     * Whether the agent is active for this bus core system
     */
    private Boolean isActive;
    
    /**
     * Whether this is the primary bus core system for the agent
     */
    private Boolean isPrimary;
}
