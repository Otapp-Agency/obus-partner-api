package com.obuspartners.modules.partner_management.repository;

import com.obuspartners.modules.partner_management.domain.entity.PartnerApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PartnerApiKey entity operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface PartnerApiKeyRepository extends JpaRepository<PartnerApiKey, Long> {

    /**
     * Find API key by the actual API key string
     */
    Optional<PartnerApiKey> findByApiKey(String apiKey);

    /**
     * Check if API key exists
     */
    boolean existsByApiKey(String apiKey);

    /**
     * Find all API keys for a specific partner
     */
    List<PartnerApiKey> findByPartnerUidOrderByCreatedAtDesc(String partnerUid);

    /**
     * Find active API keys for a specific partner
     */
    List<PartnerApiKey> findByPartnerUidAndIsActiveTrueOrderByCreatedAtDesc(String partnerUid);

    /**
     * Find primary API key for a specific partner
     */
    Optional<PartnerApiKey> findByPartnerUidAndIsPrimaryTrue(String partnerUid);

    /**
     * Find API keys by environment
     */
    List<PartnerApiKey> findByPartnerUidAndEnvironmentOrderByCreatedAtDesc(String partnerUid, String environment);

    /**
     * Find API keys by permission
     */
    @Query("SELECT pak FROM PartnerApiKey pak JOIN pak.permissions p WHERE pak.partner.uid = :partnerUid AND p = :permission")
    List<PartnerApiKey> findByPartnerUidAndPermission(@Param("partnerUid") String partnerUid, @Param("permission") String permission);

    /**
     * Find expired API keys
     */
    @Query("SELECT pak FROM PartnerApiKey pak WHERE pak.expiresAt < :now AND pak.isActive = true")
    List<PartnerApiKey> findExpiredApiKeys(@Param("now") LocalDateTime now);

    /**
     * Find API keys that haven't been used recently
     */
    @Query("SELECT pak FROM PartnerApiKey pak WHERE pak.lastUsedAt < :cutoffDate AND pak.isActive = true")
    List<PartnerApiKey> findUnusedApiKeys(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count active API keys for a partner
     */
    long countByPartnerUidAndIsActiveTrue(String partnerUid);

    /**
     * Find API key by UID
     */
    Optional<PartnerApiKey> findByUid(String uid);

    /**
     * Check if partner has any active API keys
     */
    boolean existsByPartnerUidAndIsActiveTrue(String partnerUid);

    /**
     * Find API keys by key name
     */
    List<PartnerApiKey> findByPartnerUidAndKeyNameOrderByCreatedAtDesc(String partnerUid, String keyName);

    /**
     * Find API keys created by a specific user
     */
    List<PartnerApiKey> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find API keys updated by a specific user
     */
    List<PartnerApiKey> findByUpdatedByOrderByUpdatedAtDesc(String updatedBy);
}
