package com.obuspartners.modules.partner_management.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Partner API Key entity for managing multiple API keys per partner
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Entity
@Table(name = "partner_api_keys", 
       uniqueConstraints = @UniqueConstraint(columnNames = "api_key"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", unique = true, nullable = false, length = 26)
    @NotBlank(message = "UID is required")
    @Size(max = 26, message = "UID must not exceed 26 characters")
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    @NotNull(message = "Partner is required")
    private Partner partner;

    @Column(name = "key_name", nullable = false, length = 100)
    @NotBlank(message = "Key name is required")
    @Size(max = 100, message = "Key name must not exceed 100 characters")
    private String keyName;

    @Column(name = "api_key", unique = true, nullable = false, length = 100)
    @NotBlank(message = "API key is required")
    @Size(max = 100, message = "API key must not exceed 100 characters")
    private String apiKey;

    @Column(name = "api_secret", nullable = false, length = 200)
    @NotBlank(message = "API secret is required")
    @Size(max = 200, message = "API secret must not exceed 200 characters")
    private String apiSecret;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Column(name = "environment", length = 50)
    @Size(max = 50, message = "Environment must not exceed 50 characters")
    private String environment; // "production", "staging", "development"

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "partner_api_key_permissions", 
                    joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "permission")
    private Set<String> permissions; // ["READ", "WRITE", "ADMIN", "AGENT_REGISTER"]

    @Column(name = "is_active", nullable = false)
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;

    @Column(name = "is_primary", nullable = false)
    @NotNull(message = "Primary status is required")
    private Boolean isPrimary = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "usage_count")
    private Long usageCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    private String updatedBy;

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsable() {
        return isActive && !isExpired();
    }

    public void incrementUsage() {
        this.usageCount = (this.usageCount == null ? 0L : this.usageCount) + 1;
        this.lastUsedAt = LocalDateTime.now();
    }

    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean isProductionKey() {
        return "production".equalsIgnoreCase(environment);
    }

    public boolean isStagingKey() {
        return "staging".equalsIgnoreCase(environment);
    }

    public boolean isDevelopmentKey() {
        return "development".equalsIgnoreCase(environment);
    }
}
