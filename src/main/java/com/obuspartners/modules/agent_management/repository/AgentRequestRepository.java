package com.obuspartners.modules.agent_management.repository;

import com.obuspartners.modules.agent_management.domain.entity.AgentRequest;
import com.obuspartners.modules.agent_management.domain.entity.GroupAgent;
import com.obuspartners.modules.agent_management.domain.enums.AgentRequestStatus;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AgentRequest entity
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface AgentRequestRepository extends JpaRepository<AgentRequest, Long> {

    /**
     * Find agent request by UID
     */
    Optional<AgentRequest> findByUid(String uid);

    /**
     * Find agent request by partner and partner agent number
     */
    Optional<AgentRequest> findByPartnerAndPartnerAgentNumber(Partner partner, String partnerAgentNumber);

    /**
     * Find agent request by partner and MSISDN
     */
    Optional<AgentRequest> findByPartnerAndMsisdn(Partner partner, String msisdn);

    /**
     * Check if partner agent number exists for a partner
     */
    boolean existsByPartnerAndPartnerAgentNumber(Partner partner, String partnerAgentNumber);

    /**
     * Check if MSISDN exists for a partner
     */
    boolean existsByPartnerAndMsisdn(Partner partner, String msisdn);

    /**
     * Find agent request by group agent and partner agent number
     */
    Optional<AgentRequest> findByGroupAgentAndPartnerAgentNumber(GroupAgent groupAgent, String partnerAgentNumber);

    /**
     * Find agent request by group agent and MSISDN
     */
    Optional<AgentRequest> findByGroupAgentAndMsisdn(GroupAgent groupAgent, String msisdn);

    /**
     * Check if partner agent number exists for a group agent
     */
    boolean existsByGroupAgentAndPartnerAgentNumber(GroupAgent groupAgent, String partnerAgentNumber);

    /**
     * Check if MSISDN exists for a group agent
     */
    boolean existsByGroupAgentAndMsisdn(GroupAgent groupAgent, String msisdn);

    /**
     * Find agent requests by status
     */
    List<AgentRequest> findByStatus(AgentRequestStatus status);

    /**
     * Find agent requests by partner
     */
    List<AgentRequest> findByPartner(Partner partner);

    /**
     * Find agent requests by partner with pagination
     */
    Page<AgentRequest> findByPartner(Partner partner, Pageable pageable);

    /**
     * Find agent requests by status with pagination
     */
    Page<AgentRequest> findByStatus(AgentRequestStatus status, Pageable pageable);

    /**
     * Find expired agent requests
     */
    @Query("SELECT ar FROM AgentRequest ar WHERE ar.expiresAt < :now AND ar.status = 'PENDING'")
    List<AgentRequest> findExpiredRequests(@Param("now") LocalDateTime now);

    /**
     * Find agent requests by verification reference number
     */
    Optional<AgentRequest> findByVerificationReferenceNumber(String verificationReferenceNumber);

    /**
     * Count agent requests by status
     */
    long countByStatus(AgentRequestStatus status);

    /**
     * Count agent requests by partner
     */
    long countByPartner(Partner partner);

    /**
     * Find agent requests created between dates
     */
    @Query("SELECT ar FROM AgentRequest ar WHERE ar.createdAt BETWEEN :startDate AND :endDate")
    List<AgentRequest> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * Find agent requests by business name containing (case insensitive)
     */
    @Query("SELECT ar FROM AgentRequest ar WHERE LOWER(ar.businessName) LIKE LOWER(CONCAT('%', :businessName, '%'))")
    List<AgentRequest> findByBusinessNameContainingIgnoreCase(@Param("businessName") String businessName);

    /**
     * Find agent requests by contact person containing (case insensitive)
     */
    @Query("SELECT ar FROM AgentRequest ar WHERE LOWER(ar.contactPerson) LIKE LOWER(CONCAT('%', :contactPerson, '%'))")
    List<AgentRequest> findByContactPersonContainingIgnoreCase(@Param("contactPerson") String contactPerson);
}
