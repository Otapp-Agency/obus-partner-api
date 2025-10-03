package com.obuspartners.modules.agent_management.repository;

import com.obuspartners.modules.agent_management.domain.entity.GroupAgent;
import com.obuspartners.modules.agent_management.domain.enums.GroupAgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.GroupAgentType;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GroupAgent entity
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface GroupAgentRepository extends JpaRepository<GroupAgent, Long> {

    /**
     * Find group agent by UID
     */
    Optional<GroupAgent> findByUid(String uid);

    /**
     * Find group agent by code within a partner
     */
    Optional<GroupAgent> findByPartnerAndCode(Partner partner, String code);

    /**
     * Find group agent by external system identifier within a partner
     */
    Optional<GroupAgent> findByPartnerAndExternalSystemIdentifier(Partner partner, String externalSystemIdentifier);

    /**
     * Find all group agents by partner
     */
    List<GroupAgent> findByPartner(Partner partner);

    /**
     * Find all group agents by partner with pagination
     */
    Page<GroupAgent> findByPartner(Partner partner, Pageable pageable);

    /**
     * Find all group agents by partner and status
     */
    List<GroupAgent> findByPartnerAndStatus(Partner partner, GroupAgentStatus status);

    /**
     * Find all group agents by partner and type
     */
    List<GroupAgent> findByPartnerAndType(Partner partner, GroupAgentType type);

    /**
     * Find all group agents by status
     */
    List<GroupAgent> findByStatus(GroupAgentStatus status);

    /**
     * Find all group agents by type
     */
    List<GroupAgent> findByType(GroupAgentType type);

    /**
     * Find active group agents by partner
     */
    @Query("SELECT ga FROM GroupAgent ga WHERE ga.partner = :partner AND ga.status = 'ACTIVE'")
    List<GroupAgent> findActiveByPartner(@Param("partner") Partner partner);

    /**
     * Find group agents by partner UID
     */
    @Query("SELECT ga FROM GroupAgent ga WHERE ga.partner.uid = :partnerUid")
    List<GroupAgent> findByPartnerUid(@Param("partnerUid") String partnerUid);

    /**
     * Find group agents by partner UID with pagination
     */
    @Query("SELECT ga FROM GroupAgent ga WHERE ga.partner.uid = :partnerUid")
    Page<GroupAgent> findByPartnerUid(@Param("partnerUid") String partnerUid, Pageable pageable);

    /**
     * Find active group agents by partner UID
     */
    @Query("SELECT ga FROM GroupAgent ga WHERE ga.partner.uid = :partnerUid AND ga.status = 'ACTIVE'")
    List<GroupAgent> findActiveByPartnerUid(@Param("partnerUid") String partnerUid);

    /**
     * Check if code exists for a partner
     */
    boolean existsByPartnerAndCode(Partner partner, String code);

    /**
     * Check if external system identifier exists for a partner
     */
    boolean existsByPartnerAndExternalSystemIdentifier(Partner partner, String externalSystemIdentifier);

    /**
     * Count group agents by partner
     */
    long countByPartner(Partner partner);

    /**
     * Count active group agents by partner
     */
    @Query("SELECT COUNT(ga) FROM GroupAgent ga WHERE ga.partner = :partner AND ga.status = 'ACTIVE'")
    long countActiveByPartner(@Param("partner") Partner partner);

    /**
     * Find group agents with agents count
     */
    @Query("SELECT ga, COUNT(a) FROM GroupAgent ga LEFT JOIN ga.agents a WHERE ga.partner = :partner GROUP BY ga")
    List<Object[]> findWithAgentCountByPartner(@Param("partner") Partner partner);

    /**
     * Find group agents with bus core systems count
     */
    @Query("SELECT ga, COUNT(cbs) FROM GroupAgent ga LEFT JOIN ga.coreBusSystems cbs WHERE ga.partner = :partner GROUP BY ga")
    List<Object[]> findWithCoreBusSystemCountByPartner(@Param("partner") Partner partner);

    /**
     * Find group agents by bus core system
     */
    @Query("SELECT ga FROM GroupAgent ga JOIN ga.coreBusSystems cbs WHERE cbs.busCoreSystem = :busCoreSystem")
    List<GroupAgent> findByBusCoreSystem(@Param("busCoreSystem") BusCoreSystem busCoreSystem);

    /**
     * Find group agents by bus core system and status
     */
    @Query("SELECT ga FROM GroupAgent ga JOIN ga.coreBusSystems cbs WHERE cbs.busCoreSystem = :busCoreSystem AND ga.status = :status")
    List<GroupAgent> findByBusCoreSystemAndStatus(@Param("busCoreSystem") BusCoreSystem busCoreSystem, @Param("status") GroupAgentStatus status);

    /**
     * Find group agents with recent activity
     */
    @Query("SELECT ga FROM GroupAgent ga WHERE ga.lastActivityDate >= :since ORDER BY ga.lastActivityDate DESC")
    List<GroupAgent> findWithRecentActivity(@Param("since") java.time.LocalDateTime since);

    /**
     * Find group agents by search term (name, code, or external system identifier)
     */
    @Query("SELECT ga FROM GroupAgent ga WHERE ga.partner = :partner AND " +
           "(LOWER(ga.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ga.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ga.externalSystemIdentifier) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<GroupAgent> findByPartnerAndSearchTerm(@Param("partner") Partner partner, @Param("searchTerm") String searchTerm);

    /**
     * Find group agents by search term with pagination
     */
    @Query("SELECT ga FROM GroupAgent ga WHERE ga.partner = :partner AND " +
           "(LOWER(ga.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ga.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ga.externalSystemIdentifier) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<GroupAgent> findByPartnerAndSearchTerm(@Param("partner") Partner partner, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search group agents across all partners by search term with pagination
     */
    @Query("SELECT ga FROM GroupAgent ga WHERE " +
           "(LOWER(ga.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ga.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ga.externalSystemIdentifier) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<GroupAgent> searchGroupAgents(@Param("searchTerm") String searchTerm, Pageable pageable);
}
