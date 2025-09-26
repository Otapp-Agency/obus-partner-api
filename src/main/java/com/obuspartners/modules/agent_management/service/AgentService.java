package com.obuspartners.modules.agent_management.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.obuspartners.modules.agent_management.domain.dto.*;
import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Agent management operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface AgentService {

    // CRUD Operations

    /**
     * Create a new agent
     * 
     * @param createRequest the agent creation request
     * @return the created agent response
     */
    AgentResponseDto createAgent(CreateAgentRequestDto createRequest);

    /**
     * Update an existing agent
     * 
     * @param uid the UID of the agent to update
     * @param updateRequest the agent update request
     * @return the updated agent response
     */
    AgentResponseDto updateAgent(String uid, UpdateAgentRequestDto updateRequest);

    /**
     * Update an existing agent by code
     * 
     * @param code the code of the agent to update
     * @param updateRequest the agent update request
     * @return the updated agent response
     */
    AgentResponseDto updateAgentByCode(String code, UpdateAgentRequestDto updateRequest);

    // Retrieve Operations

    /**
     * Get agent by UID
     * 
     * @param uid the agent UID
     * @return Optional containing the agent response if found
     */
    Optional<AgentResponseDto> getAgent(String uid);

    /**
     * Get agent by ID
     * 
     * @param agentId the agent ID
     * @return Optional containing the agent response if found
     */
    Optional<AgentResponseDto> getAgentById(Long agentId);

    /**
     * Get agent by code
     * 
     * @param code the agent code
     * @return Optional containing the agent response if found
     */
    Optional<AgentResponseDto> getAgentByCode(String code);

    /**
     * Get agent by business email
     * 
     * @param businessEmail the business email address
     * @return Optional containing the agent response if found
     */
    Optional<AgentResponseDto> getAgentByBusinessEmail(String businessEmail);

    /**
     * Get agent by phone number
     * 
     * @param phoneNumber the phone number
     * @return Optional containing the agent response if found
     */
    Optional<AgentResponseDto> getAgentByPhoneNumber(String phoneNumber);

    // Status Management

    /**
     * Approve an agent (change status from PENDING_APPROVAL to ACTIVE)
     * 
     * @param uid the UID of the agent to approve
     * @return the updated agent response
     */
    AgentResponseDto approveAgent(String uid);

    /**
     * Approve an agent by code
     * 
     * @param code the code of the agent to approve
     * @return the updated agent response
     */
    AgentResponseDto approveAgentByCode(String code);

    /**
     * Reject an agent (change status to REJECTED)
     * 
     * @param uid the UID of the agent to reject
     * @return the updated agent response
     */
    AgentResponseDto rejectAgent(String uid);

    /**
     * Suspend an agent
     * 
     * @param uid the UID of the agent to suspend
     * @return the updated agent response
     */
    AgentResponseDto suspendAgent(String uid);

    /**
     * Activate an agent
     * 
     * @param uid the UID of the agent to activate
     * @return the updated agent response
     */
    AgentResponseDto activateAgent(String uid);

    /**
     * Deactivate an agent
     * 
     * @param uid the UID of the agent to deactivate
     * @return the updated agent response
     */
    AgentResponseDto deactivateAgent(String uid);

    /**
     * Lock an agent account
     * 
     * @param uid the UID of the agent to lock
     * @return the updated agent response
     */
    AgentResponseDto lockAgent(String uid);

    /**
     * Update agent status
     * 
     * @param uid the UID of the agent
     * @param status the new status
     * @return the updated agent response
     */
    AgentResponseDto updateAgentStatus(String uid, AgentStatus status);

    // Agent Hierarchy Management

    /**
     * Assign a super agent to a sub-agent
     * 
     * @param subAgentUid the UID of the sub-agent
     * @param superAgentUid the UID of the super agent
     * @return the updated sub-agent response
     */
    AgentResponseDto assignSuperAgent(String subAgentUid, String superAgentUid);

    /**
     * Remove super agent assignment from a sub-agent
     * 
     * @param subAgentUid the UID of the sub-agent
     * @return the updated sub-agent response
     */
    AgentResponseDto removeSuperAgent(String subAgentUid);

    /**
     * Get all sub-agents of a super agent
     * 
     * @param superAgentUid the UID of the super agent
     * @param pageable pagination information
     * @return Page of sub-agents
     */
    Page<AgentSummaryDto> getSubAgents(String superAgentUid, Pageable pageable);

    /**
     * Get agent hierarchy (super agent and all sub-agents)
     * 
     * @param superAgentUid the UID of the super agent
     * @return List of agents in the hierarchy
     */
    List<AgentResponseDto> getAgentHierarchy(String superAgentUid);

    // Partner-related Operations

    /**
     * Get all agents for a specific partner
     * 
     * @param partnerUid the partner UID
     * @param pageable pagination information
     * @return Page of agents belonging to the partner
     */
    Page<AgentSummaryDto> getAgentsByPartner(String partnerUid, Pageable pageable);

    /**
     * Get active agents for a specific partner
     * 
     * @param partnerUid the partner UID
     * @param pageable pagination information
     * @return Page of active agents belonging to the partner
     */
    Page<AgentSummaryDto> getActiveAgentsByPartner(String partnerUid, Pageable pageable);

    /**
     * Get super agents for a specific partner
     * 
     * @param partnerUid the partner UID
     * @param pageable pagination information
     * @return Page of super agents belonging to the partner
     */
    Page<AgentSummaryDto> getSuperAgentsByPartner(String partnerUid, Pageable pageable);

    /**
     * Count agents by partner
     * 
     * @param partnerUid the partner UID
     * @return number of agents belonging to the partner
     */
    long countAgentsByPartner(String partnerUid);

    // Query Operations

    /**
     * Get all agents with pagination
     * 
     * @param pageable pagination information
     * @return Page of agent summaries
     */
    Page<AgentSummaryDto> getAllAgents(Pageable pageable);

    /**
     * Get all agents without pagination (for assignment purposes)
     * 
     * @return List of all agent summaries
     */
    List<AgentSummaryDto> getAllAgentsForAssignment();

    /**
     * Get agents by status
     * 
     * @param status the agent status
     * @param pageable pagination information
     * @return Page of agent summaries with the specified status
     */
    Page<AgentSummaryDto> getAgentsByStatus(AgentStatus status, Pageable pageable);

    /**
     * Get agents by type
     * 
     * @param agentType the agent type
     * @param pageable pagination information
     * @return Page of agent summaries with the specified type
     */
    Page<AgentSummaryDto> getAgentsByType(AgentType agentType, Pageable pageable);

    /**
     * Get active agents
     * 
     * @param pageable pagination information
     * @return Page of active agent summaries
     */
    Page<AgentSummaryDto> getActiveAgents(Pageable pageable);

    /**
     * Get pending agents (PENDING_APPROVAL status)
     * 
     * @param pageable pagination information
     * @return Page of pending agent summaries
     */
    Page<AgentSummaryDto> getPendingAgents(Pageable pageable);

    /**
     * Search agents by business name
     * 
     * @param businessName the business name to search for
     * @param pageable pagination information
     * @return Page of agent summaries matching the search criteria
     */
    Page<AgentSummaryDto> searchAgentsByBusinessName(String businessName, Pageable pageable);

    /**
     * Search agents by contact person
     * 
     * @param contactPerson the contact person to search for
     * @param pageable pagination information
     * @return Page of agent summaries matching the search criteria
     */
    Page<AgentSummaryDto> searchAgentsByContactPerson(String contactPerson, Pageable pageable);

    /**
     * Advanced search agents with multiple criteria
     * 
     * @param searchRequest the search request with criteria
     * @return Page of agent summaries matching the search criteria
     */
    Page<AgentSummaryDto> searchAgents(AgentSearchRequestDto searchRequest);

    /**
     * Get agents by registration date range
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return Page of agents registered within the date range
     */
    Page<AgentSummaryDto> getAgentsByRegistrationDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Get agents by last activity date range
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return Page of agents with last activity within the date range
     */
    Page<AgentSummaryDto> getAgentsByLastActivityDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Validation and Existence Checks

    /**
     * Check if UID exists
     * 
     * @param uid the agent UID
     * @return true if UID exists, false otherwise
     */
    boolean existsByUid(String uid);

    /**
     * Check if code exists
     * 
     * @param code the agent code
     * @return true if code exists, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Check if business email exists
     * 
     * @param businessEmail the business email address
     * @return true if business email exists, false otherwise
     */
    boolean existsByBusinessEmail(String businessEmail);

    /**
     * Check if phone number exists
     * 
     * @param phoneNumber the phone number
     * @return true if phone number exists, false otherwise
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Check if tax ID exists
     * 
     * @param taxId the tax identification number
     * @return true if tax ID exists, false otherwise
     */
    boolean existsByTaxId(String taxId);

    /**
     * Check if license number exists
     * 
     * @param licenseNumber the license number
     * @return true if license number exists, false otherwise
     */
    boolean existsByLicenseNumber(String licenseNumber);

    /**
     * Validate agent data before creation
     * 
     * @param agent the agent to validate
     * @return true if valid, false otherwise
     */
    boolean validateAgent(Agent agent);

    /**
     * Validate super agent assignment
     * 
     * @param subAgentUid the UID of the sub-agent
     * @param superAgentUid the UID of the super agent
     * @return true if assignment is valid, false otherwise
     */
    boolean validateSuperAgentAssignment(String subAgentUid, String superAgentUid);

    // Utility Methods

    /**
     * Generate unique agent code
     * 
     * @param partnerUid the partner UID
     * @return a unique agent code
     */
    String generateAgentCode(String partnerUid);

    /**
     * Update agent last activity
     * 
     * @param uid the agent UID
     */
    void updateLastActivity(String uid);

    // Statistics and Counting

    /**
     * Count agents by status
     * 
     * @param status the agent status
     * @return number of agents with the specified status
     */
    long countAgentsByStatus(AgentStatus status);

    /**
     * Count agents by type
     * 
     * @param agentType the agent type
     * @return number of agents with the specified type
     */
    long countAgentsByType(AgentType agentType);

    /**
     * Count active agents
     * 
     * @return number of active agents
     */
    long countActiveAgents();

    /**
     * Count pending agents
     * 
     * @return number of pending agents
     */
    long countPendingAgents();

    /**
     * Count sub-agents under a super agent
     * 
     * @param superAgentUid the super agent UID
     * @return number of sub-agents
     */
    long countSubAgents(String superAgentUid);

    /**
     * Get agent statistics
     * 
     * @return agent statistics summary
     */
    AgentStatistics getAgentStatistics();

    /**
     * Get agent statistics for a specific partner
     * 
     * @param partnerUid the partner UID
     * @return agent statistics for the partner
     */
    AgentStatistics getAgentStatisticsByPartner(String partnerUid);

    // Bulk Operations

    /**
     * Bulk update agent status
     * 
     * @param agentUids list of agent UIDs to update
     * @param status the new status
     */
    void bulkUpdateAgentStatus(List<String> agentUids, AgentStatus status);

    /**
     * Bulk approve agents
     * 
     * @param agentUids list of agent UIDs to approve
     */
    void bulkApproveAgents(List<String> agentUids);

    /**
     * Bulk reject agents
     * 
     * @param agentUids list of agent UIDs to reject
     */
    void bulkRejectAgents(List<String> agentUids);

    /**
     * Agent statistics class
     */
    class AgentStatistics {
        private long totalAgents;
        private long activeAgents;
        private long pendingAgents;
        private long suspendedAgents;
        private long rejectedAgents;
        private long superAgents;
        private long subAgents;
        private long agentsByPartner;

        public AgentStatistics() {}

        public AgentStatistics(long totalAgents, long activeAgents, long pendingAgents,
                             long suspendedAgents, long rejectedAgents, long superAgents,
                             long subAgents, long agentsByPartner) {
            this.totalAgents = totalAgents;
            this.activeAgents = activeAgents;
            this.pendingAgents = pendingAgents;
            this.suspendedAgents = suspendedAgents;
            this.rejectedAgents = rejectedAgents;
            this.superAgents = superAgents;
            this.subAgents = subAgents;
            this.agentsByPartner = agentsByPartner;
        }

        // Getters and setters
        public long getTotalAgents() { return totalAgents; }
        public void setTotalAgents(long totalAgents) { this.totalAgents = totalAgents; }

        public long getActiveAgents() { return activeAgents; }
        public void setActiveAgents(long activeAgents) { this.activeAgents = activeAgents; }

        public long getPendingAgents() { return pendingAgents; }
        public void setPendingAgents(long pendingAgents) { this.pendingAgents = pendingAgents; }

        public long getSuspendedAgents() { return suspendedAgents; }
        public void setSuspendedAgents(long suspendedAgents) { this.suspendedAgents = suspendedAgents; }

        public long getRejectedAgents() { return rejectedAgents; }
        public void setRejectedAgents(long rejectedAgents) { this.rejectedAgents = rejectedAgents; }

        public long getSuperAgents() { return superAgents; }
        public void setSuperAgents(long superAgents) { this.superAgents = superAgents; }

        public long getSubAgents() { return subAgents; }
        public void setSubAgents(long subAgents) { this.subAgents = subAgents; }

        public long getAgentsByPartner() { return agentsByPartner; }
        public void setAgentsByPartner(long agentsByPartner) { this.agentsByPartner = agentsByPartner; }
    }
}
