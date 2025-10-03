package com.obuspartners.modules.agent_management.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import de.huxhorn.sulky.ulid.ULID;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;

import java.time.LocalDateTime;

/**
 * GroupAgentCoreBusSystem entity for managing bus core system credentials
 * Stores group agent-specific credentials and configurations for each bus core system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Entity
@Table(name = "group_agent_core_bus_systems", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"group_agent_id", "bus_core_system_id"}),
           @UniqueConstraint(columnNames = {"bus_core_system_id", "external_agent_identifier"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupAgentCoreBusSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", unique = true, nullable = false, updatable = false)
    private String uid;

    // Foreign Keys
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_agent_id", nullable = false)
    private GroupAgent groupAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_core_system_id", nullable = false)
    @NotNull(message = "Bus core system is required")
    private BusCoreSystem busCoreSystem;

    // External system credentials
    @Column(name = "external_agent_identifier", nullable = false)
    @NotBlank(message = "External agent identifier is required")
    @Size(max = 100, message = "External agent identifier must not exceed 100 characters")
    private String externalAgentIdentifier;

    @Column(name = "username", nullable = false)
    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    private String username;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password must not exceed 255 characters")
    private String password; // Encrypted password

    @Column(name = "txn_username")
    @Size(max = 100, message = "Transaction username must not exceed 100 characters")
    private String txnUserName; // Transaction username

    @Column(name = "txn_password")
    @Size(max = 255, message = "Transaction password must not exceed 255 characters")
    private String txnPassword; // Encrypted transaction password

    @Column(name = "api_key")
    @Size(max = 500, message = "API key must not exceed 500 characters")
    private String apiKey; // Encrypted API key

    @Column(name = "api_secret")
    @Size(max = 500, message = "API secret must not exceed 500 characters")
    private String apiSecret; // Encrypted API secret

    @Column(name = "access_token")
    @Size(max = 1000, message = "Access token must not exceed 1000 characters")
    private String accessToken; // Encrypted access token

    @Column(name = "refresh_token")
    @Size(max = 1000, message = "Refresh token must not exceed 1000 characters")
    private String refreshToken; // Encrypted refresh token

    // Configuration and status
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false; // Primary bus core system for the group agent

    @Column(name = "external_system_status")
    @Size(max = 50, message = "External system status must not exceed 50 characters")
    private String externalSystemStatus; // Status in the external system (e.g., "ACTIVE", "SUSPENDED")

    @Column(name = "external_agent_id")
    @Size(max = 100, message = "External agent ID must not exceed 100 characters")
    private String externalAgentId; // Agent ID in the external system

    // Additional configuration
    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration; // JSON configuration for external system

    @Column(name = "endpoint_url")
    @Size(max = 500, message = "Endpoint URL must not exceed 500 characters")
    private String endpointUrl;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 30;

    @Column(name = "retry_attempts")
    private Integer retryAttempts = 3;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "last_authentication_date")
    private LocalDateTime lastAuthenticationDate;

    @Column(name = "last_sync_date")
    private LocalDateTime lastSyncDate;

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // Business methods
    public boolean isActive() {
        return isActive != null && isActive;
    }

    public boolean isPrimary() {
        return isPrimary != null && isPrimary;
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastAuthentication() {
        this.lastAuthenticationDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastSync() {
        this.lastSyncDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasValidCredentials() {
        return username != null && !username.trim().isEmpty() && 
               password != null && !password.trim().isEmpty();
    }

    public boolean hasTransactionCredentials() {
        return txnUserName != null && !txnUserName.trim().isEmpty() && 
               txnPassword != null && !txnPassword.trim().isEmpty();
    }

    public boolean hasApiCredentials() {
        return apiKey != null && !apiKey.trim().isEmpty() && 
               apiSecret != null && !apiSecret.trim().isEmpty();
    }

    public boolean hasTokenCredentials() {
        return accessToken != null && !accessToken.trim().isEmpty();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void ensureUid() {
        if (uid == null) {
            uid = new ULID().nextULID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
