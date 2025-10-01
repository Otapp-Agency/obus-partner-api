package com.obuspartners.modules.agent_management.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for assigning agent to bus core system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignAgentToBusCoreSystemRequest {

    @NotNull(message = "Agent ID is required")
    private Long agentId;

    @NotNull(message = "Bus core system ID is required")
    private Long busCoreSystemId;

    @NotBlank(message = "Agent login name is required")
    private String agentLoginName;

    @NotBlank(message = "Password is required")
    private String password; // Plain text password - will be encrypted by service

    private String txnUserName;
    private String txnPassword; // Plain text transaction password - will be encrypted by service

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isPrimary = false;

    private String agentStatusInBusCore; // Status in the bus core system
    private String busCoreAgentId; // Agent ID in the bus core system
}
