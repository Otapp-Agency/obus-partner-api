package com.obuspartners.modules.agent_management.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for agent login request
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentLoginRequestDto {

    @NotBlank(message = "Agent number is required")
    private String agentNumber;

    @NotBlank(message = "Login password is required")
    private String loginPassword;
}
