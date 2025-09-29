package com.obuspartners.modules.agent_management.repository;

import com.obuspartners.modules.agent_management.domain.entity.PartnerAgentVerification;
import com.obuspartners.modules.agent_management.domain.enums.VerificationStatus;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.agent_management.domain.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PartnerAgentVerification entity
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface PartnerAgentVerificationRepository extends JpaRepository<PartnerAgentVerification, Long> {

    /**
     * Find verification by partner and agent
     */
    Optional<PartnerAgentVerification> findByPartnerAndAgent(Partner partner, Agent agent);

    /**
     * Find verification by partner and request reference number
     */
    Optional<PartnerAgentVerification> findByPartnerAndRequestReferenceNumber(Partner partner, String requestReferenceNumber);

    /**
     * Find all verifications by partner
     */
    List<PartnerAgentVerification> findByPartner(Partner partner);

    /**
     * Find all verifications by agent
     */
    List<PartnerAgentVerification> findByAgent(Agent agent);

    /**
     * Find verifications by status
     */
    List<PartnerAgentVerification> findByVerificationStatus(VerificationStatus status);

    /**
     * Find verifications by partner and status
     */
    List<PartnerAgentVerification> findByPartnerAndVerificationStatus(Partner partner, VerificationStatus status);

    /**
     * Check if verification exists for partner and agent
     */
    boolean existsByPartnerAndAgent(Partner partner, Agent agent);

    /**
     * Check if request reference number exists for partner
     */
    boolean existsByPartnerAndRequestReferenceNumber(Partner partner, String requestReferenceNumber);

    /**
     * Find pending verifications
     */
    @Query("SELECT v FROM PartnerAgentVerification v WHERE v.verificationStatus = 'PENDING' ORDER BY v.requestedAt ASC")
    List<PartnerAgentVerification> findPendingVerifications();

    /**
     * Find expired verifications
     */
    @Query("SELECT v FROM PartnerAgentVerification v WHERE v.expiresAt IS NOT NULL AND v.expiresAt < CURRENT_TIMESTAMP AND v.verificationStatus = 'PENDING'")
    List<PartnerAgentVerification> findExpiredVerifications();
}
