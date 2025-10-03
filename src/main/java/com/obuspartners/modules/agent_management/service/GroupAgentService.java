package com.obuspartners.modules.agent_management.service;

import com.obuspartners.modules.agent_management.domain.entity.GroupAgent;
import com.obuspartners.modules.agent_management.domain.enums.GroupAgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.GroupAgentType;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for GroupAgent management operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface GroupAgentService {

    /**
     * Create a new GroupAgent
     * 
     * @param groupAgent the group agent to create
     * @return the created group agent
     */
    GroupAgent createGroupAgent(GroupAgent groupAgent);

    /**
     * Create a new GroupAgent with partner
     * 
     * @param partner the partner to create group agent for
     * @param name the group agent name
     * @param code the group agent code
     * @param externalSystemIdentifier the external system identifier
     * @param type the group agent type
     * @return the created group agent
     */
    GroupAgent createGroupAgent(Partner partner, String name, String code, String externalSystemIdentifier, GroupAgentType type);

    /**
     * Update an existing GroupAgent
     * 
     * @param groupAgent the group agent to update
     * @return the updated group agent
     */
    GroupAgent updateGroupAgent(GroupAgent groupAgent);

    /**
     * Get GroupAgent by ID
     * 
     * @param id the group agent ID
     * @return the group agent if found
     */
    Optional<GroupAgent> getGroupAgentById(Long id);

    /**
     * Get GroupAgent by UID
     * 
     * @param uid the group agent UID
     * @return the group agent if found
     */
    Optional<GroupAgent> getGroupAgentByUid(String uid);

    /**
     * Get GroupAgent by partner and code
     * 
     * @param partner the partner
     * @param code the group agent code
     * @return the group agent if found
     */
    Optional<GroupAgent> getGroupAgentByPartnerAndCode(Partner partner, String code);

    /**
     * Get all GroupAgents by partner
     * 
     * @param partner the partner
     * @return list of group agents
     */
    List<GroupAgent> getGroupAgentsByPartner(Partner partner);

    /**
     * Get all GroupAgents by partner with pagination
     * 
     * @param partner the partner
     * @param pageable pagination parameters
     * @return page of group agents
     */
    Page<GroupAgent> getGroupAgentsByPartner(Partner partner, Pageable pageable);

    /**
     * Get all GroupAgents by partner UID
     * 
     * @param partnerUid the partner UID
     * @return list of group agents
     */
    List<GroupAgent> getGroupAgentsByPartnerUid(String partnerUid);

    /**
     * Get all GroupAgents by partner UID with pagination
     * 
     * @param partnerUid the partner UID
     * @param pageable pagination parameters
     * @return page of group agents
     */
    Page<GroupAgent> getGroupAgentsByPartnerUid(String partnerUid, Pageable pageable);

    /**
     * Get all GroupAgents by status
     * 
     * @param status the group agent status
     * @return list of group agents
     */
    List<GroupAgent> getGroupAgentsByStatus(GroupAgentStatus status);

    /**
     * Get all GroupAgents by type
     * 
     * @param type the group agent type
     * @return list of group agents
     */
    List<GroupAgent> getGroupAgentsByType(GroupAgentType type);

    /**
     * Get all active GroupAgents (for assignment purposes)
     * 
     * @return list of active group agents
     */
    List<GroupAgent> getAllActiveGroupAgents();

    /**
     * Get active GroupAgents by partner
     * 
     * @param partner the partner
     * @return list of active group agents
     */
    List<GroupAgent> getActiveGroupAgentsByPartner(Partner partner);

    /**
     * Activate a GroupAgent
     * 
     * @param groupAgent the group agent to activate
     * @return the activated group agent
     */
    GroupAgent activateGroupAgent(GroupAgent groupAgent);

    /**
     * Suspend a GroupAgent
     * 
     * @param groupAgent the group agent to suspend
     * @return the suspended group agent
     */
    GroupAgent suspendGroupAgent(GroupAgent groupAgent);

    /**
     * Deactivate a GroupAgent
     * 
     * @param groupAgent the group agent to deactivate
     * @return the deactivated group agent
     */
    GroupAgent deactivateGroupAgent(GroupAgent groupAgent);

    /**
     * Delete a GroupAgent
     * 
     * @param id the group agent ID
     */
    void deleteGroupAgent(Long id);

    /**
     * Delete a GroupAgent by UID
     * 
     * @param uid the group agent UID
     */
    void deleteGroupAgentByUid(String uid);

    /**
     * Check if GroupAgent code exists for partner
     * 
     * @param partner the partner
     * @param code the group agent code
     * @return true if exists, false otherwise
     */
    boolean existsByPartnerAndCode(Partner partner, String code);

    /**
     * Check if external system identifier exists for partner
     * 
     * @param partner the partner
     * @param externalSystemIdentifier the external system identifier
     * @return true if exists, false otherwise
     */
    boolean existsByPartnerAndExternalSystemIdentifier(Partner partner, String externalSystemIdentifier);

    /**
     * Count GroupAgents by partner
     * 
     * @param partner the partner
     * @return count of group agents
     */
    long countByPartner(Partner partner);

    /**
     * Count active GroupAgents by partner
     * 
     * @param partner the partner
     * @return count of active group agents
     */
    long countActiveByPartner(Partner partner);

    /**
     * Search GroupAgents by partner and search term
     * 
     * @param partner the partner
     * @param searchTerm the search term
     * @return list of matching group agents
     */
    List<GroupAgent> searchGroupAgentsByPartner(Partner partner, String searchTerm);

    /**
     * Search GroupAgents by partner and search term with pagination
     * 
     * @param partner the partner
     * @param searchTerm the search term
     * @param pageable pagination parameters
     * @return page of matching group agents
     */
    Page<GroupAgent> searchGroupAgentsByPartner(Partner partner, String searchTerm, Pageable pageable);

    /**
     * Update GroupAgent last activity
     * 
     * @param groupAgent the group agent
     */
    void updateLastActivity(GroupAgent groupAgent);

    /**
     * Validate GroupAgent data
     * 
     * @param groupAgent the group agent to validate
     * @return true if valid, false otherwise
     */
    boolean validateGroupAgent(GroupAgent groupAgent);

    /**
     * Check if GroupAgent can be deleted
     * 
     * @param groupAgent the group agent
     * @return true if can be deleted, false otherwise
     */
    boolean canDeleteGroupAgent(GroupAgent groupAgent);

    /**
     * Get GroupAgents with recent activity
     * 
     * @param since the date since when to look for activity
     * @return list of group agents with recent activity
     */
    List<GroupAgent> getGroupAgentsWithRecentActivity(java.time.LocalDateTime since);

    /**
     * Get all GroupAgents with pagination
     * 
     * @param pageable pagination parameters
     * @return page of all group agents
     */
    Page<GroupAgent> getAllGroupAgents(Pageable pageable);

    /**
     * Search GroupAgents across all partners
     * 
     * @param searchTerm the search term
     * @param pageable pagination parameters
     * @return page of matching group agents
     */
    Page<GroupAgent> searchGroupAgents(String searchTerm, Pageable pageable);
}
