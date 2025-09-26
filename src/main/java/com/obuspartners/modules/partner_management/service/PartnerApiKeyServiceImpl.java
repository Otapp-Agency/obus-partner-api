package com.obuspartners.modules.partner_management.service;

import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.domain.entity.PartnerApiKey;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;
import com.obuspartners.modules.partner_management.repository.PartnerApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.huxhorn.sulky.ulid.ULID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of PartnerApiKeyService for managing Partner API keys
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class PartnerApiKeyServiceImpl implements PartnerApiKeyService {

    private final PartnerRepository partnerRepository;
    private final PartnerApiKeyRepository partnerApiKeyRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ULID ulid = new ULID();

    public PartnerApiKeyServiceImpl(PartnerRepository partnerRepository, 
                                   PartnerApiKeyRepository partnerApiKeyRepository,
                                   @Lazy BCryptPasswordEncoder passwordEncoder) {
        this.partnerRepository = partnerRepository;
        this.partnerApiKeyRepository = partnerApiKeyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ApiKeyInfo generateApiKey(String partnerUid, String keyName, String description, 
                                   String environment, Set<String> permissions, LocalDateTime expiresAt, 
                                   boolean isPrimary, String createdBy) {
        log.info("Generating API key for partner: {} with name: {}", partnerUid, keyName);
        
        Partner partner = findPartnerByUid(partnerUid);
        
        // If this is set as primary, unset other primary keys
        if (isPrimary) {
            partnerApiKeyRepository.findByPartnerUidAndIsPrimaryTrue(partnerUid)
                .ifPresent(existingPrimary -> {
                    existingPrimary.setIsPrimary(false);
                    existingPrimary.setUpdatedBy(createdBy);
                    partnerApiKeyRepository.save(existingPrimary);
                });
        }

        String newApiKeyUid = ulid.nextULID();
        String newApiKey = generateSecureApiKey();
        String newApiSecret = generateSecureApiSecret();

        PartnerApiKey partnerApiKey = new PartnerApiKey();
        partnerApiKey.setUid(newApiKeyUid);
        partnerApiKey.setPartner(partner);
        partnerApiKey.setKeyName(keyName);
        partnerApiKey.setApiKey(newApiKey);
        partnerApiKey.setApiSecret(passwordEncoder.encode(newApiSecret));
        partnerApiKey.setDescription(description);
        partnerApiKey.setEnvironment(environment);
        partnerApiKey.setPermissions(permissions);
        partnerApiKey.setIsActive(true);
        partnerApiKey.setIsPrimary(isPrimary);
        partnerApiKey.setExpiresAt(expiresAt);
        partnerApiKey.setCreatedBy(createdBy);
        partnerApiKey.setUpdatedBy(createdBy);

        partnerApiKeyRepository.save(partnerApiKey);
        log.info("API key generated successfully with UID: {}", newApiKeyUid);

        ApiKeyInfo apiKeyInfo = new ApiKeyInfo();
        apiKeyInfo.setApiKeyUid(newApiKeyUid);
        apiKeyInfo.setApiKey(newApiKey);
        apiKeyInfo.setApiSecret(newApiSecret);
        apiKeyInfo.setPartnerUid(partnerUid);
        apiKeyInfo.setKeyName(keyName);
        apiKeyInfo.setDescription(description);
        apiKeyInfo.setEnvironment(environment);
        apiKeyInfo.setPermissions(permissions);
        apiKeyInfo.setExpiresAt(expiresAt);
        apiKeyInfo.setPrimary(isPrimary);

        return apiKeyInfo;
    }

    @Override
    @Transactional
    public ApiKeyInfo generateApiKey(String partnerUid) {
        log.info("Generating simple API key for partner: {}", partnerUid);
        
        return generateApiKey(
            partnerUid,
            "Primary API Key",
            "Auto-generated primary API key",
            "production",
            Set.of("READ", "WRITE", "AGENT_REGISTER"),
            null,
            true,
            "system"
        );
    }

    @Override
    @Transactional
    public ApiKeyInfo regenerateApiKey(String apiKeyUid, String updatedBy) {
        log.info("Regenerating API key: {}", apiKeyUid);
        
        PartnerApiKey partnerApiKey = findApiKeyByUid(apiKeyUid);
        
        String newApiKey = generateSecureApiKey();
        String newApiSecret = generateSecureApiSecret();

        partnerApiKey.setApiKey(newApiKey);
        partnerApiKey.setApiSecret(passwordEncoder.encode(newApiSecret));
        partnerApiKey.setUpdatedBy(updatedBy);
        partnerApiKey.setLastUsedAt(LocalDateTime.now());

        partnerApiKeyRepository.save(partnerApiKey);
        log.info("API key regenerated successfully");

        ApiKeyInfo apiKeyInfo = new ApiKeyInfo();
        apiKeyInfo.setApiKeyUid(apiKeyUid);
        apiKeyInfo.setApiKey(newApiKey);
        apiKeyInfo.setApiSecret(newApiSecret);
        apiKeyInfo.setPartnerUid(partnerApiKey.getPartner().getUid());
        apiKeyInfo.setKeyName(partnerApiKey.getKeyName());
        apiKeyInfo.setDescription(partnerApiKey.getDescription());
        apiKeyInfo.setEnvironment(partnerApiKey.getEnvironment());
        apiKeyInfo.setPermissions(partnerApiKey.getPermissions());
        apiKeyInfo.setExpiresAt(partnerApiKey.getExpiresAt());
        apiKeyInfo.setPrimary(partnerApiKey.getIsPrimary());

        return apiKeyInfo;
    }

    @Override
    @Transactional
    public void enableApiKey(String apiKeyUid, String updatedBy) {
        log.info("Enabling API key: {}", apiKeyUid);
        
        PartnerApiKey partnerApiKey = findApiKeyByUid(apiKeyUid);
        partnerApiKey.setIsActive(true);
        partnerApiKey.setUpdatedBy(updatedBy);
        
        partnerApiKeyRepository.save(partnerApiKey);
        log.info("API key enabled successfully");
    }

    @Override
    @Transactional
    public void disableApiKey(String apiKeyUid, String updatedBy) {
        log.info("Disabling API key: {}", apiKeyUid);
        
        PartnerApiKey partnerApiKey = findApiKeyByUid(apiKeyUid);
        String apiKey = partnerApiKey.getApiKey(); // Get API key for cache eviction
        
        partnerApiKey.setIsActive(false);
        partnerApiKey.setUpdatedAt(LocalDateTime.now());
        partnerApiKey.setUpdatedBy(updatedBy);
        
        partnerApiKeyRepository.save(partnerApiKey);
        
        // Evict from cache
        evictApiKeyFromCache(apiKey);
        
        log.info("API key disabled successfully");
    }

    @Override
    @Transactional
    public void revokeApiKey(String apiKeyUid, String updatedBy) {
        log.info("Revoking API key: {}", apiKeyUid);
        
        PartnerApiKey partnerApiKey = findApiKeyByUid(apiKeyUid);
        String apiKey = partnerApiKey.getApiKey(); // Get API key for cache eviction
        
        partnerApiKeyRepository.delete(partnerApiKey);
        
        // Evict from cache
        evictApiKeyFromCache(apiKey);
        
        log.info("API key revoked successfully");
    }

    @Override
    public ApiKeyStatus getApiKeyStatus(String apiKeyUid) {
        log.debug("Getting API key status for: {}", apiKeyUid);
        
        Optional<PartnerApiKey> partnerApiKeyOpt = partnerApiKeyRepository.findByUid(apiKeyUid);
        
        if (partnerApiKeyOpt.isEmpty()) {
            return new ApiKeyStatus(false, false, null, null);
        }
        
        PartnerApiKey partnerApiKey = partnerApiKeyOpt.get();
        ApiKeyStatus status = new ApiKeyStatus();
        status.setExists(true);
        status.setActive(partnerApiKey.getIsActive());
        status.setExpired(partnerApiKey.isExpired());
        status.setUsable(partnerApiKey.isUsable());
        status.setCreatedAt(partnerApiKey.getCreatedAt());
        status.setLastUsedAt(partnerApiKey.getLastUsedAt());
        status.setExpiresAt(partnerApiKey.getExpiresAt());
        status.setUsageCount(partnerApiKey.getUsageCount());
        status.setKeyName(partnerApiKey.getKeyName());
        status.setEnvironment(partnerApiKey.getEnvironment());
        status.setPermissions(partnerApiKey.getPermissions());
        
        return status;
    }

    @Override
    public List<ApiKeySummary> getPartnerApiKeys(String partnerUid) {
        log.debug("Getting all API keys for partner: {}", partnerUid);
        
        // First validate that the partner exists
        Partner partner = findPartnerByUid(partnerUid);
        
        List<PartnerApiKey> apiKeys = partnerApiKeyRepository.findByPartnerUidOrderByCreatedAtDesc(partnerUid);
        
        return apiKeys.stream()
            .map(this::mapToApiKeySummary)
            .collect(Collectors.toList());
    }

    @Override
    public List<ApiKeySummary> getActivePartnerApiKeys(String partnerUid) {
        log.debug("Getting active API keys for partner: {}", partnerUid);
        
        // First validate that the partner exists
        Partner partner = findPartnerByUid(partnerUid);
        
        List<PartnerApiKey> apiKeys = partnerApiKeyRepository.findByPartnerUidAndIsActiveTrueOrderByCreatedAtDesc(partnerUid);
        
        return apiKeys.stream()
            .map(this::mapToApiKeySummary)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setPrimaryApiKey(String apiKeyUid, String updatedBy) {
        log.info("Setting primary API key: {}", apiKeyUid);
        
        PartnerApiKey partnerApiKey = findApiKeyByUid(apiKeyUid);
        
        // Unset other primary keys for this partner
        partnerApiKeyRepository.findByPartnerUidAndIsPrimaryTrue(partnerApiKey.getPartner().getUid())
            .ifPresent(existingPrimary -> {
                existingPrimary.setIsPrimary(false);
                existingPrimary.setUpdatedBy(updatedBy);
                partnerApiKeyRepository.save(existingPrimary);
            });
        
        // Set this as primary
        partnerApiKey.setIsPrimary(true);
        partnerApiKey.setUpdatedBy(updatedBy);
        
        partnerApiKeyRepository.save(partnerApiKey);
        log.info("Primary API key set successfully");
    }

    @Override
    @Transactional
    public void updateApiKeyPermissions(String apiKeyUid, Set<String> permissions, String updatedBy) {
        log.info("Updating API key permissions for: {}", apiKeyUid);
        
        PartnerApiKey partnerApiKey = findApiKeyByUid(apiKeyUid);
        partnerApiKey.setPermissions(permissions);
        partnerApiKey.setUpdatedBy(updatedBy);
        
        partnerApiKeyRepository.save(partnerApiKey);
        log.info("API key permissions updated successfully");
    }

    @Override
    @Transactional
    public void updateApiKeyExpiration(String apiKeyUid, LocalDateTime expiresAt, String updatedBy) {
        log.info("Updating API key expiration for: {}", apiKeyUid);
        
        PartnerApiKey partnerApiKey = findApiKeyByUid(apiKeyUid);
        partnerApiKey.setExpiresAt(expiresAt);
        partnerApiKey.setUpdatedBy(updatedBy);
        
        partnerApiKeyRepository.save(partnerApiKey);
        log.info("API key expiration updated successfully");
    }

    @Override
    public Optional<ApiKeyInfo> validateApiKeyAndSecret(String apiKey, String apiSecret) {
        log.debug("Validating API key and secret: {}", apiKey);
        
        Optional<PartnerApiKey> partnerApiKeyOpt = partnerApiKeyRepository.findByApiKey(apiKey);
        
        if (partnerApiKeyOpt.isEmpty()) {
            log.warn("API key not found: {}", apiKey);
            return Optional.empty();
        }
        
        PartnerApiKey partnerApiKey = partnerApiKeyOpt.get();
        
        if (!partnerApiKey.isUsable()) {
            log.warn("API key found but not usable: {}", apiKey);
            return Optional.empty();
        }
        
        // Verify the secret
        if (!passwordEncoder.matches(apiSecret, partnerApiKey.getApiSecret())) {
            log.warn("API secret does not match for key: {}", apiKey);
            return Optional.empty();
        }
        
        ApiKeyInfo apiKeyInfo = new ApiKeyInfo();
        apiKeyInfo.setApiKeyUid(partnerApiKey.getUid());
        apiKeyInfo.setApiKey(partnerApiKey.getApiKey());
        apiKeyInfo.setPartnerUid(partnerApiKey.getPartner().getUid());
        apiKeyInfo.setKeyName(partnerApiKey.getKeyName());
        apiKeyInfo.setDescription(partnerApiKey.getDescription());
        apiKeyInfo.setEnvironment(partnerApiKey.getEnvironment());
        apiKeyInfo.setPermissions(partnerApiKey.getPermissions());
        apiKeyInfo.setExpiresAt(partnerApiKey.getExpiresAt());
        apiKeyInfo.setPrimary(partnerApiKey.getIsPrimary());
        
        return Optional.of(apiKeyInfo);
    }

    @Override
    @Transactional
    public void recordApiKeyUsage(String apiKey) {
        log.debug("Recording API key usage: {}", apiKey);
        
        Optional<PartnerApiKey> partnerApiKeyOpt = partnerApiKeyRepository.findByApiKey(apiKey);
        
        if (partnerApiKeyOpt.isPresent()) {
            PartnerApiKey partnerApiKey = partnerApiKeyOpt.get();
            partnerApiKey.incrementUsage();
            partnerApiKeyRepository.save(partnerApiKey);
        }
    }

    @Override
    public List<ApiKeySummary> getExpiredApiKeys() {
        log.debug("Getting expired API keys");
        
        List<PartnerApiKey> expiredKeys = partnerApiKeyRepository.findExpiredApiKeys(LocalDateTime.now());
        
        return expiredKeys.stream()
            .map(this::mapToApiKeySummary)
            .collect(Collectors.toList());
    }

    @Override
    public List<ApiKeySummary> getUnusedApiKeys(int days) {
        log.debug("Getting unused API keys for {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<PartnerApiKey> unusedKeys = partnerApiKeyRepository.findUnusedApiKeys(cutoffDate);
        
        return unusedKeys.stream()
            .map(this::mapToApiKeySummary)
            .collect(Collectors.toList());
    }

    // Helper methods
    private Partner findPartnerByUid(String partnerUid) {
        return partnerRepository.findByUid(partnerUid)
            .orElseThrow(() -> new IllegalArgumentException("Partner not found with UID: " + partnerUid));
    }

    private PartnerApiKey findApiKeyByUid(String apiKeyUid) {
        return partnerApiKeyRepository.findByUid(apiKeyUid)
            .orElseThrow(() -> new IllegalArgumentException("API key not found with UID: " + apiKeyUid));
    }

    private ApiKeySummary mapToApiKeySummary(PartnerApiKey partnerApiKey) {
        ApiKeySummary summary = new ApiKeySummary();
        summary.setApiKeyUid(partnerApiKey.getUid());
        summary.setKeyName(partnerApiKey.getKeyName());
        summary.setDescription(partnerApiKey.getDescription());
        summary.setEnvironment(partnerApiKey.getEnvironment());
        summary.setPermissions(partnerApiKey.getPermissions());
        summary.setActive(partnerApiKey.getIsActive());
        summary.setPrimary(partnerApiKey.getIsPrimary());
        summary.setCreatedAt(partnerApiKey.getCreatedAt());
        summary.setLastUsedAt(partnerApiKey.getLastUsedAt());
        summary.setExpiresAt(partnerApiKey.getExpiresAt());
        summary.setUsageCount(partnerApiKey.getUsageCount());
        return summary;
    }

    /**
     * Helper method to evict API key from cache
     */
    @CacheEvict(value = "apiKeyValidation", key = "#apiKey")
    private void evictApiKeyFromCache(String apiKey) {
        log.debug("Evicting API key from cache: {}", apiKey);
    }

    /**
     * Generate secure API key with industry-standard format
     * Format: ak_live_[32-char-random-string]
     */
    private String generateSecureApiKey() {
        // Generate 32-character random string using secure random
        String randomPart = UUID.randomUUID().toString().replace("-", "") + 
                           UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return "ak_live_" + randomPart;
    }

    /**
     * Generate secure API secret (40 characters for optimal security/usability)
     */
    private String generateSecureApiSecret() {
        // Generate 40-character secret using secure random
        String secret1 = UUID.randomUUID().toString().replace("-", "");
        String secret2 = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return secret1 + secret2;
    }
}