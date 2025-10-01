package com.obuspartners.modules.agent_management.service;

import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.domain.entity.AgentBusCoreSystem;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import com.obuspartners.modules.agent_management.domain.dto.AssignAgentToBusCoreSystemRequest;
import com.obuspartners.modules.agent_management.domain.dto.AgentBusCoreSystemResponseDto;
import com.obuspartners.modules.agent_management.domain.dto.DecryptedAgentCredentials;
import com.obuspartners.modules.agent_management.domain.dto.UpdateAgentBusCoreSystemRequest;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for AgentBusCoreSystem management
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface AgentBusCoreSystemService {

    /**
     * Assign an agent to a bus core system
     */
    AgentBusCoreSystemResponseDto assignAgentToBusCoreSystem(AssignAgentToBusCoreSystemRequest request);

    /**
     * Find agent bus core system by ID
     * 
     * @param id the agent bus core system ID
     * @return Optional containing the agent bus core system response if found
     */
    java.util.Optional<AgentBusCoreSystemResponseDto> findById(Long id);

    /**
     * Find agent bus core system by UID
     * 
     * @param uid the agent bus core system UID
     * @return Optional containing the agent bus core system response if found
     */
    java.util.Optional<AgentBusCoreSystemResponseDto> findByUid(String uid);

    /**
     * Update agent configuration for a bus core system
     */
    AgentBusCoreSystemResponseDto updateAgentBusCoreSystem(Long id, UpdateAgentBusCoreSystemRequest request);

    /**
     * Update agent configuration for a bus core system by UID
     */
    AgentBusCoreSystemResponseDto updateAgentBusCoreSystemByUid(String uid, UpdateAgentBusCoreSystemRequest request);

    /**
     * Get agent configuration for a specific bus core system
     */
    Optional<AgentBusCoreSystem> getAgentBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem);

    /**
     * Get all bus core systems assigned to an agent
     */
    List<AgentBusCoreSystemResponseDto> getBusCoreSystemsByAgent(Agent agent);
    
    /**
     * Get all bus core systems assigned to an agent by agent ID
     */
    List<AgentBusCoreSystemResponseDto> getBusCoreSystemsByAgentId(Long agentId);

    /**
     * Get all bus core systems assigned to an agent by agent UID
     */
    List<AgentBusCoreSystemResponseDto> getBusCoreSystemsByAgentUid(String agentUid);

    /**
     * Get all agents assigned to a bus core system
     */
    List<AgentBusCoreSystemResponseDto> getAgentsByBusCoreSystem(BusCoreSystem busCoreSystem);
    
    /**
     * Get all agents assigned to a bus core system by bus core system ID
     */
    List<AgentBusCoreSystemResponseDto> getAgentsByBusCoreSystemId(Long busCoreSystemId);

    /**
     * Get active bus core systems for an agent
     */
    List<AgentBusCoreSystemResponseDto> getActiveBusCoreSystemsByAgent(Agent agent);

    /**
     * Get primary bus core system for an agent
     */
    Optional<AgentBusCoreSystemResponseDto> getPrimaryBusCoreSystemByAgent(Agent agent);

    /**
     * Set primary bus core system for an agent
     */
    AgentBusCoreSystemResponseDto setPrimaryBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem);

    /**
     * Activate/deactivate agent for a bus core system
     */
    AgentBusCoreSystemResponseDto setAgentActiveStatus(Long id, boolean isActive);

    /**
     * Activate/deactivate agent for a bus core system by UID
     */
    AgentBusCoreSystemResponseDto setAgentActiveStatusByUid(String uid, boolean isActive);

    /**
     * Update agent credentials for a bus core system
     */
    AgentBusCoreSystemResponseDto updateAgentCredentials(Long id, String agentLoginName, String password, String txnPassword);

    /**
     * Authenticate agent for a specific bus core system
     */
    boolean authenticateAgentForBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem, String agentLoginName, String password);

    /**
     * Get agent credentials for bus core system operations
     */
    Optional<AgentBusCoreSystem> getAgentCredentialsForBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem);

    /**
     * Get decrypted agent credentials for a specific bus core system
     * This method returns the credentials with decrypted passwords for use by the bus core system
     */
    Optional<DecryptedAgentCredentials> getDecryptedAgentCredentialsForBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem);

    /**
     * Remove agent from bus core system
     */
    void removeAgentFromBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem);

    /**
     * Check if agent can perform specific operation on bus core system
     */
    boolean canAgentPerformOperation(Agent agent, BusCoreSystem busCoreSystem, String operation);

    /**
     * Get agent permissions for a bus core system
     */
    AgentBusCoreSystemResponseDto getAgentPermissions(Agent agent, BusCoreSystem busCoreSystem);

    /**
     * Update agent permissions for a bus core system
     */
    AgentBusCoreSystemResponseDto updateAgentPermissions(Long id, UpdateAgentBusCoreSystemRequest request);

    /**
     * Find all AgentBusCoreSystem records for key rotation
     */
    List<AgentBusCoreSystem> findAllForKeyRotation();

    /**
     * Save AgentBusCoreSystem entity
     */
    AgentBusCoreSystem save(AgentBusCoreSystem agentBusCoreSystem);

    /**
     * Convert AgentBusCoreSystem entity to response DTO
     */
    AgentBusCoreSystemResponseDto convertToResponseDto(AgentBusCoreSystem agentBusCoreSystem);

    /**
     * Find agent bus core system entity by UID (for internal operations)
     */
    Optional<AgentBusCoreSystem> findEntityByUid(String uid);
}
