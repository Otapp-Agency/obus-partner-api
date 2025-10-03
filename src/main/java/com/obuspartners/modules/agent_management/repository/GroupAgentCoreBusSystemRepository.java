package com.obuspartners.modules.agent_management.repository;

import com.obuspartners.modules.agent_management.domain.entity.GroupAgent;
import com.obuspartners.modules.agent_management.domain.entity.GroupAgentCoreBusSystem;
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
 * Repository interface for GroupAgentCoreBusSystem entity
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface GroupAgentCoreBusSystemRepository extends JpaRepository<GroupAgentCoreBusSystem, Long> {

    /**
     * Find by UID
     */
    Optional<GroupAgentCoreBusSystem> findByUid(String uid);

    /**
     * Find by group agent and bus core system
     */
    Optional<GroupAgentCoreBusSystem> findByGroupAgentAndBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem);

    /**
     * Find all bus core systems by group agent
     */
    List<GroupAgentCoreBusSystem> findByGroupAgent(GroupAgent groupAgent);

    /**
     * Find all bus core systems by group agent with pagination
     */
    Page<GroupAgentCoreBusSystem> findByGroupAgent(GroupAgent groupAgent, Pageable pageable);

    /**
     * Find active bus core systems by group agent
     */
    List<GroupAgentCoreBusSystem> findByGroupAgentAndIsActiveTrue(GroupAgent groupAgent);

    /**
     * Find primary bus core system by group agent
     */
    Optional<GroupAgentCoreBusSystem> findByGroupAgentAndIsPrimaryTrue(GroupAgent groupAgent);

    /**
     * Find by bus core system
     */
    List<GroupAgentCoreBusSystem> findByBusCoreSystem(BusCoreSystem busCoreSystem);

    /**
     * Find by bus core system and active status
     */
    List<GroupAgentCoreBusSystem> findByBusCoreSystemAndIsActiveTrue(BusCoreSystem busCoreSystem);

    /**
     * Find by external agent identifier and bus core system
     */
    Optional<GroupAgentCoreBusSystem> findByBusCoreSystemAndExternalAgentIdentifier(BusCoreSystem busCoreSystem, String externalAgentIdentifier);

    /**
     * Find by group agent UID
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.groupAgent.uid = :groupAgentUid")
    List<GroupAgentCoreBusSystem> findByGroupAgentUid(@Param("groupAgentUid") String groupAgentUid);

    /**
     * Find active bus core systems by group agent UID
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.groupAgent.uid = :groupAgentUid AND cbs.isActive = true")
    List<GroupAgentCoreBusSystem> findActiveByGroupAgentUid(@Param("groupAgentUid") String groupAgentUid);

    /**
     * Find primary bus core system by group agent UID
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.groupAgent.uid = :groupAgentUid AND cbs.isPrimary = true")
    Optional<GroupAgentCoreBusSystem> findPrimaryByGroupAgentUid(@Param("groupAgentUid") String groupAgentUid);

    /**
     * Find by partner UID and bus core system
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.groupAgent.partner.uid = :partnerUid AND cbs.busCoreSystem = :busCoreSystem")
    List<GroupAgentCoreBusSystem> findByPartnerUidAndBusCoreSystem(@Param("partnerUid") String partnerUid, @Param("busCoreSystem") BusCoreSystem busCoreSystem);

    /**
     * Find active bus core systems by partner UID and bus core system
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.groupAgent.partner.uid = :partnerUid AND cbs.busCoreSystem = :busCoreSystem AND cbs.isActive = true")
    List<GroupAgentCoreBusSystem> findActiveByPartnerUidAndBusCoreSystem(@Param("partnerUid") String partnerUid, @Param("busCoreSystem") BusCoreSystem busCoreSystem);

    /**
     * Check if bus core system exists for group agent
     */
    boolean existsByGroupAgentAndBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem);

    /**
     * Check if external agent identifier exists for bus core system
     */
    boolean existsByBusCoreSystemAndExternalAgentIdentifier(BusCoreSystem busCoreSystem, String externalAgentIdentifier);

    /**
     * Count bus core systems by group agent
     */
    long countByGroupAgent(GroupAgent groupAgent);

    /**
     * Count active bus core systems by group agent
     */
    long countByGroupAgentAndIsActiveTrue(GroupAgent groupAgent);

    /**
     * Find bus core systems with recent authentication
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.lastAuthenticationDate >= :since ORDER BY cbs.lastAuthenticationDate DESC")
    List<GroupAgentCoreBusSystem> findWithRecentAuthentication(@Param("since") java.time.LocalDateTime since);

    /**
     * Find bus core systems with recent sync
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.lastSyncDate >= :since ORDER BY cbs.lastSyncDate DESC")
    List<GroupAgentCoreBusSystem> findWithRecentSync(@Param("since") java.time.LocalDateTime since);

    /**
     * Find bus core systems by bus core system name
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE LOWER(cbs.busCoreSystem.name) LIKE LOWER(CONCAT('%', :systemName, '%'))")
    List<GroupAgentCoreBusSystem> findByBusCoreSystemNameContainingIgnoreCase(@Param("systemName") String systemName);

    /**
     * Find bus core systems by group agent and bus core system name
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.groupAgent = :groupAgent AND LOWER(cbs.busCoreSystem.name) LIKE LOWER(CONCAT('%', :systemName, '%'))")
    List<GroupAgentCoreBusSystem> findByGroupAgentAndBusCoreSystemNameContainingIgnoreCase(@Param("groupAgent") GroupAgent groupAgent, @Param("systemName") String systemName);

    /**
     * Find bus core systems by search term (bus core system name, external agent identifier, or username)
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.groupAgent = :groupAgent AND " +
           "(LOWER(cbs.busCoreSystem.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cbs.externalAgentIdentifier) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cbs.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<GroupAgentCoreBusSystem> findByGroupAgentAndSearchTerm(@Param("groupAgent") GroupAgent groupAgent, @Param("searchTerm") String searchTerm);

    /**
     * Find bus core systems by search term with pagination
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.groupAgent = :groupAgent AND " +
           "(LOWER(cbs.busCoreSystem.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cbs.externalAgentIdentifier) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cbs.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<GroupAgentCoreBusSystem> findByGroupAgentAndSearchTerm(@Param("groupAgent") GroupAgent groupAgent, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find bus core systems by partner UID
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.groupAgent.partner.uid = :partnerUid")
    List<GroupAgentCoreBusSystem> findByPartnerUid(@Param("partnerUid") String partnerUid);

    /**
     * Find active bus core systems by partner UID
     */
    @Query("SELECT cbs FROM GroupAgentCoreBusSystem cbs WHERE cbs.groupAgent.partner.uid = :partnerUid AND cbs.isActive = true")
    List<GroupAgentCoreBusSystem> findActiveByPartnerUid(@Param("partnerUid") String partnerUid);
}
