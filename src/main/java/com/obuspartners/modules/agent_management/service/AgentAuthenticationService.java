package com.obuspartners.modules.agent_management.service;

import com.obuspartners.modules.agent_management.domain.dto.AgentLoginRequestDto;
import com.obuspartners.modules.agent_management.domain.dto.AgentLoginResponseDto;

/**
 * Service interface for Agent authentication operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface AgentAuthenticationService {

    /**
     * Authenticate agent with login credentials
     * 
     * @param loginRequest the login request containing username and password
     * @return authentication response with token and agent details
     */
    AgentLoginResponseDto authenticateAgent(AgentLoginRequestDto loginRequest);

    /**
     * Validate agent token
     * 
     * @param token the authentication token
     * @return true if token is valid, false otherwise
     */
    boolean validateAgentToken(String token);

    /**
     * Get agent from token
     * 
     * @param token the authentication token
     * @return agent response if token is valid, null otherwise
     */
    com.obuspartners.modules.agent_management.domain.dto.AgentResponseDto getAgentFromToken(String token);
}
