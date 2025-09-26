package com.obuspartners.modules.partner_management.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.Optional;

/**
 * Service interface for managing Partner API keys
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface PartnerApiKeyService {

    /**
     * Generate a new API key for a partner
     * 
     * @param partnerUid the partner UID
     * @param keyName the name for the API key
     * @param description optional description
     * @param environment the environment (production, staging, development)
     * @param permissions the permissions for this key
     * @param expiresAt optional expiration date
     * @param isPrimary whether this should be the primary key
     * @param createdBy who created this key
     * @return API key information
     */
    ApiKeyInfo generateApiKey(String partnerUid, String keyName, String description, 
                             String environment, Set<String> permissions, LocalDateTime expiresAt, 
                             boolean isPrimary, String createdBy);

    /**
     * Generate a simple API key (backward compatibility)
     */
    ApiKeyInfo generateApiKey(String partnerUid);

    /**
     * Regenerate an existing API key
     * 
     * @param apiKeyUid the API key UID to regenerate
     * @param updatedBy who is updating this key
     * @return new API key information
     */
    ApiKeyInfo regenerateApiKey(String apiKeyUid, String updatedBy);

    /**
     * Enable an API key
     * 
     * @param apiKeyUid the API key UID
     * @param updatedBy who is updating this key
     */
    void enableApiKey(String apiKeyUid, String updatedBy);

    /**
     * Disable an API key
     * 
     * @param apiKeyUid the API key UID
     * @param updatedBy who is updating this key
     */
    void disableApiKey(String apiKeyUid, String updatedBy);

    /**
     * Revoke (delete) an API key
     * 
     * @param apiKeyUid the API key UID
     * @param updatedBy who is revoking this key
     */
    void revokeApiKey(String apiKeyUid, String updatedBy);

    /**
     * Get API key status
     * 
     * @param apiKeyUid the API key UID
     * @return API key status information
     */
    ApiKeyStatus getApiKeyStatus(String apiKeyUid);

    /**
     * Get all API keys for a partner
     * 
     * @param partnerUid the partner UID
     * @return list of API keys
     */
    List<ApiKeySummary> getPartnerApiKeys(String partnerUid);

    /**
     * Get active API keys for a partner
     * 
     * @param partnerUid the partner UID
     * @return list of active API keys
     */
    List<ApiKeySummary> getActivePartnerApiKeys(String partnerUid);

    /**
     * Set primary API key
     * 
     * @param apiKeyUid the API key UID to set as primary
     * @param updatedBy who is updating this key
     */
    void setPrimaryApiKey(String apiKeyUid, String updatedBy);

    /**
     * Update API key permissions
     * 
     * @param apiKeyUid the API key UID
     * @param permissions new permissions
     * @param updatedBy who is updating this key
     */
    void updateApiKeyPermissions(String apiKeyUid, Set<String> permissions, String updatedBy);

    /**
     * Update API key expiration
     * 
     * @param apiKeyUid the API key UID
     * @param expiresAt new expiration date
     * @param updatedBy who is updating this key
     */
    void updateApiKeyExpiration(String apiKeyUid, LocalDateTime expiresAt, String updatedBy);

    /**
     * Validate API key and secret
     * 
     * @param apiKey the API key string
     * @param apiSecret the API secret
     * @return API key information if valid, empty otherwise
     */
    Optional<ApiKeyInfo> validateApiKeyAndSecret(String apiKey, String apiSecret);

    /**
     * Record API key usage
     * 
     * @param apiKey the API key string
     */
    void recordApiKeyUsage(String apiKey);

    /**
     * Get expired API keys
     * 
     * @return list of expired API keys
     */
    List<ApiKeySummary> getExpiredApiKeys();

    /**
     * Get unused API keys (not used in specified days)
     * 
     * @param days number of days
     * @return list of unused API keys
     */
    List<ApiKeySummary> getUnusedApiKeys(int days);

    /**
     * API Key Information class
     */
    class ApiKeyInfo {
        private String apiKeyUid;
        private String apiKey;
        private String apiSecret;
        private String partnerUid;
        private String keyName;
        private String description;
        private String environment;
        private Set<String> permissions;
        private LocalDateTime expiresAt;
        private boolean isPrimary;

        public ApiKeyInfo() {}

        public ApiKeyInfo(String apiKey, String apiSecret, String partnerUid) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
            this.partnerUid = partnerUid;
        }

        // Getters and setters
        public String getApiKeyUid() { return apiKeyUid; }
        public void setApiKeyUid(String apiKeyUid) { this.apiKeyUid = apiKeyUid; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getApiSecret() { return apiSecret; }
        public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }

        public String getPartnerUid() { return partnerUid; }
        public void setPartnerUid(String partnerUid) { this.partnerUid = partnerUid; }

        public String getKeyName() { return keyName; }
        public void setKeyName(String keyName) { this.keyName = keyName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }

        public Set<String> getPermissions() { return permissions; }
        public void setPermissions(Set<String> permissions) { this.permissions = permissions; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        public boolean isPrimary() { return isPrimary; }
        public void setPrimary(boolean primary) { isPrimary = primary; }
    }

    /**
     * API Key Status class
     */
    class ApiKeyStatus {
        private boolean exists;
        private boolean isActive;
        private boolean isExpired;
        private boolean isUsable;
        private LocalDateTime createdAt;
        private LocalDateTime lastUsedAt;
        private LocalDateTime expiresAt;
        private Long usageCount;
        private String keyName;
        private String environment;
        private Set<String> permissions;

        public ApiKeyStatus() {}

        public ApiKeyStatus(boolean exists, boolean isActive, LocalDateTime createdAt, LocalDateTime lastUsedAt) {
            this.exists = exists;
            this.isActive = isActive;
            this.createdAt = createdAt;
            this.lastUsedAt = lastUsedAt;
        }

        // Getters and setters
        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }

        public boolean isExpired() { return isExpired; }
        public void setExpired(boolean expired) { isExpired = expired; }

        public boolean isUsable() { return isUsable; }
        public void setUsable(boolean usable) { isUsable = usable; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getLastUsedAt() { return lastUsedAt; }
        public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        public Long getUsageCount() { return usageCount; }
        public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }

        public String getKeyName() { return keyName; }
        public void setKeyName(String keyName) { this.keyName = keyName; }

        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }

        public Set<String> getPermissions() { return permissions; }
        public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    }

    /**
     * API Key Summary class
     */
    class ApiKeySummary {
        private String apiKeyUid;
        private String keyName;
        private String description;
        private String environment;
        private Set<String> permissions;
        private boolean isActive;
        private boolean isPrimary;
        private LocalDateTime createdAt;
        private LocalDateTime lastUsedAt;
        private LocalDateTime expiresAt;
        private Long usageCount;

        public ApiKeySummary() {}

        // Getters and setters
        public String getApiKeyUid() { return apiKeyUid; }
        public void setApiKeyUid(String apiKeyUid) { this.apiKeyUid = apiKeyUid; }

        public String getKeyName() { return keyName; }
        public void setKeyName(String keyName) { this.keyName = keyName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }

        public Set<String> getPermissions() { return permissions; }
        public void setPermissions(Set<String> permissions) { this.permissions = permissions; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }

        public boolean isPrimary() { return isPrimary; }
        public void setPrimary(boolean primary) { isPrimary = primary; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getLastUsedAt() { return lastUsedAt; }
        public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

        public Long getUsageCount() { return usageCount; }
        public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
    }

    // DTO for creating API keys
    class CreateApiKeyRequestDto {
        @NotBlank(message = "Key name is required")
        @Size(max = 100, message = "Key name must not exceed 100 characters")
        private String keyName;
        
        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;
        
        @Size(max = 50, message = "Environment must not exceed 50 characters")
        private String environment;
        
        private Set<String> permissions;
        
        private LocalDateTime expiresAt;
        
        private Boolean isPrimary = false;

        // Getters and setters
        public String getKeyName() { return keyName; }
        public void setKeyName(String keyName) { this.keyName = keyName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        
        public Set<String> getPermissions() { return permissions; }
        public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
        
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        
        public Boolean getIsPrimary() { return isPrimary; }
        public void setIsPrimary(Boolean primary) { isPrimary = primary; }
    }
}