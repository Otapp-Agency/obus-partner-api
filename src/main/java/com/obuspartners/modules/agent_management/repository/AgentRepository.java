package com.obuspartners.modules.agent_management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;
import com.obuspartners.modules.partner_management.domain.entity.Partner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Agent entity
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    /**
     * Find agent by unique UID
     * 
     * @param uid the agent UID
     * @return Optional containing the agent if found
     */
    Optional<Agent> findByUid(String uid);

    /**
     * Find agent by unique code
     * 
     * @param code the agent code
     * @return Optional containing the agent if found
     */
    Optional<Agent> findByCode(String code);

    /**
     * Find agent by partner and partner agent number
     * 
     * @param partner the partner entity
     * @param partnerAgentNumber the partner agent number
     * @return Optional containing the agent if found
     */
    Optional<Agent> findByPartnerAndPartnerAgentNumber(Partner partner, String partnerAgentNumber);

    /**
     * Find agent by login username
     * 
     * @param loginUsername the login username
     * @return Optional containing the agent if found
     */
    Optional<Agent> findByLoginUsername(String loginUsername);

    /**
     * Find agent by business email
     * 
     * @param businessEmail the business email address
     * @return Optional containing the agent if found
     */
    Optional<Agent> findByBusinessEmail(String businessEmail);

    /**
     * Find agent by phone number
     * 
     * @param phoneNumber the phone number
     * @return Optional containing the agent if found
     */
    Optional<Agent> findByPhoneNumber(String phoneNumber);

    /**
     * Find agent by tax ID
     * 
     * @param taxId the tax identification number
     * @return Optional containing the agent if found
     */
    Optional<Agent> findByTaxId(String taxId);

    /**
     * Find agent by license number
     * 
     * @param licenseNumber the license number
     * @return Optional containing the agent if found
     */
    Optional<Agent> findByLicenseNumber(String licenseNumber);

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
     * Check if partner agent number exists for a partner
     * 
     * @param partner the partner entity
     * @param partnerAgentNumber the partner agent number
     * @return true if partner agent number exists for the partner, false otherwise
     */
    boolean existsByPartnerAndPartnerAgentNumber(Partner partner, String partnerAgentNumber);

    /**
     * Check if login username exists
     * 
     * @param loginUsername the login username
     * @return true if login username exists, false otherwise
     */
    boolean existsByLoginUsername(String loginUsername);

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

    // Partner-related queries

    /**
     * Find all agents belonging to a specific partner
     * 
     * @param partner the partner entity
     * @param pageable pagination information
     * @return Page of agents belonging to the partner
     */
    Page<Agent> findByPartner(Partner partner, Pageable pageable);

    /**
     * Find agents by partner ID
     * 
     * @param partnerId the partner ID
     * @param pageable pagination information
     * @return Page of agents belonging to the partner
     */
    Page<Agent> findByPartnerId(Long partnerId, Pageable pageable);

    /**
     * Count agents by partner
     * 
     * @param partner the partner entity
     * @return number of agents belonging to the partner
     */
    long countByPartner(Partner partner);

    /**
     * Count agents by partner ID
     * 
     * @param partnerId the partner ID
     * @return number of agents belonging to the partner
     */
    long countByPartnerId(Long partnerId);

    // Status-related queries

    /**
     * Find agents by status
     * 
     * @param status the agent status
     * @param pageable pagination information
     * @return Page of agents with the specified status
     */
    Page<Agent> findByStatus(AgentStatus status, Pageable pageable);

    /**
     * Find agents by partner and status
     * 
     * @param partner the partner entity
     * @param status the agent status
     * @param pageable pagination information
     * @return Page of agents matching both criteria
     */
    Page<Agent> findByPartnerAndStatus(Partner partner, AgentStatus status, Pageable pageable);

    /**
     * Count agents by status
     * 
     * @param status the agent status
     * @return number of agents with the specified status
     */
    long countByStatus(AgentStatus status);

    /**
     * Count agents by partner and status
     * 
     * @param partner the partner entity
     * @param status the agent status
     * @return number of agents matching both criteria
     */
    long countByPartnerAndStatus(Partner partner, AgentStatus status);

    // Type-related queries

    /**
     * Find agents by type
     * 
     * @param agentType the agent type
     * @param pageable pagination information
     * @return Page of agents with the specified type
     */
    Page<Agent> findByAgentType(AgentType agentType, Pageable pageable);

    /**
     * Find agents by partner and type
     * 
     * @param partner the partner entity
     * @param agentType the agent type
     * @param pageable pagination information
     * @return Page of agents matching both criteria
     */
    Page<Agent> findByPartnerAndAgentType(Partner partner, AgentType agentType, Pageable pageable);

    /**
     * Count agents by type
     * 
     * @param agentType the agent type
     * @return number of agents with the specified type
     */
    long countByAgentType(AgentType agentType);

    // Super agent and sub-agent queries

    /**
     * Find all sub-agents of a specific super agent
     * 
     * @param superAgent the super agent entity
     * @param pageable pagination information
     * @return Page of sub-agents under the super agent
     */
    Page<Agent> findBySuperAgent(Agent superAgent, Pageable pageable);

    /**
     * Find sub-agents by super agent ID
     * 
     * @param superAgentId the super agent ID
     * @param pageable pagination information
     * @return Page of sub-agents under the super agent
     */
    Page<Agent> findBySuperAgentId(Long superAgentId, Pageable pageable);

    /**
     * Count sub-agents under a super agent
     * 
     * @param superAgent the super agent entity
     * @return number of sub-agents under the super agent
     */
    long countBySuperAgent(Agent superAgent);

    /**
     * Find all super agents for a partner
     * 
     * @param partner the partner entity
     * @param pageable pagination information
     * @return Page of super agents belonging to the partner
     */
    @Query("SELECT a FROM Agent a WHERE a.partner = :partner AND a.agentType = 'SUPER_AGENT'")
    Page<Agent> findSuperAgentsByPartner(@Param("partner") Partner partner, Pageable pageable);

    /**
     * Find agents without super agent (not sub-agents or orphaned sub-agents)
     * 
     * @param partner the partner entity
     * @param pageable pagination information
     * @return Page of agents without super agent
     */
    Page<Agent> findByPartnerAndSuperAgentIsNull(Partner partner, Pageable pageable);

    // Search and filter queries

    /**
     * Search agents by business name containing the given text
     * 
     * @param businessName the business name to search for
     * @param pageable pagination information
     * @return Page of agents with business names containing the search text
     */
    Page<Agent> findByBusinessNameContainingIgnoreCase(String businessName, Pageable pageable);

    /**
     * Search agents by contact person containing the given text
     * 
     * @param contactPerson the contact person to search for
     * @param pageable pagination information
     * @return Page of agents with contact person names containing the search text
     */
    Page<Agent> findByContactPersonContainingIgnoreCase(String contactPerson, Pageable pageable);

    /**
     * Find agents by registration date range
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return Page of agents registered within the date range
     */
    Page<Agent> findByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find agents by last activity date range
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return Page of agents with last activity within the date range
     */
    Page<Agent> findByLastActivityDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Combined search queries

    /**
     * Find agents by partner, status, and type
     * 
     * @param partner the partner entity
     * @param status the agent status
     * @param agentType the agent type
     * @param pageable pagination information
     * @return Page of agents matching all criteria
     */
    Page<Agent> findByPartnerAndStatusAndAgentType(Partner partner, AgentStatus status, AgentType agentType, Pageable pageable);

    /**
     * Custom query to search agents by multiple criteria
     * 
     * @param partnerId partner ID filter (optional)
     * @param businessName business name search term (optional)
     * @param status status filter (optional)
     * @param agentType type filter (optional)
     * @param pageable pagination information
     * @return Page of agents matching the search criteria
     */
    @Query("SELECT a FROM Agent a WHERE " +
           "(:partnerId IS NULL OR a.partner.id = :partnerId) AND " +
           "(:businessName IS NULL OR LOWER(a.businessName) LIKE LOWER(CONCAT('%', :businessName, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:agentType IS NULL OR a.agentType = :agentType)")
    Page<Agent> searchAgents(@Param("partnerId") Long partnerId,
                            @Param("businessName") String businessName,
                            @Param("status") AgentStatus status,
                            @Param("agentType") AgentType agentType,
                            Pageable pageable);

    /**
     * Find active agents (status = ACTIVE)
     * 
     * @param pageable pagination information
     * @return Page of active agents
     */
    @Query("SELECT a FROM Agent a WHERE a.status = 'ACTIVE'")
    Page<Agent> findActiveAgents(Pageable pageable);

    /**
     * Find pending agents (status = PENDING_APPROVAL)
     * 
     * @param pageable pagination information
     * @return Page of pending agents
     */
    @Query("SELECT a FROM Agent a WHERE a.status = 'PENDING_APPROVAL'")
    Page<Agent> findPendingAgents(Pageable pageable);

    /**
     * Find agents by partner with active status
     * 
     * @param partner the partner entity
     * @param pageable pagination information
     * @return Page of active agents for the partner
     */
    @Query("SELECT a FROM Agent a WHERE a.partner = :partner AND a.status = 'ACTIVE'")
    Page<Agent> findActiveAgentsByPartner(@Param("partner") Partner partner, Pageable pageable);

    /**
     * Get agent hierarchy tree (super agent and its sub-agents)
     * 
     * @param superAgent the super agent entity
     * @return List of agents in the hierarchy
     */
    @Query("SELECT a FROM Agent a WHERE a.superAgent = :superAgent OR a = :superAgent ORDER BY a.agentType, a.code")
    List<Agent> findAgentHierarchy(@Param("superAgent") Agent superAgent);
}
