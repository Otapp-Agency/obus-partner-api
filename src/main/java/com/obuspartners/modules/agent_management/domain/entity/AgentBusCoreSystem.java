package com.obuspartners.modules.agent_management.domain.entity;

import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import de.huxhorn.sulky.ulid.ULID;

import java.time.LocalDateTime;

/**
 * Pivot entity for Agent-BusCoreSystem relationship
 * Stores agent-specific credentials and configurations for each bus core system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Entity
@Table(name = "agent_bus_core_systems", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"agent_id", "bus_core_system_id"}),
           @UniqueConstraint(columnNames = {"agent_login_name", "bus_core_system_id"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentBusCoreSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", unique = true, nullable = false, updatable = false)
    private String uid;

    // Foreign Keys
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_core_system_id", nullable = false)
    private BusCoreSystem busCoreSystem;

    // Agent Authentication Credentials for this Bus Core System
    @Column(name = "agent_login_name", nullable = false)
    private String agentLoginName;

    @Column(name = "password", nullable = false)
    private String password; // Encrypted password

    @Column(name = "txn_user_name")
    private String txnUserName;

    @Column(name = "txn_password")
    private String txnPassword; // Encrypted transaction password

    // Basic Status and Configuration
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false; // Primary bus core system for the agent

    @Column(name = "agent_status_in_bus_core")
    private String agentStatusInBusCore; // Status in the bus core system (e.g., "ACTIVE", "SUSPENDED")

    @Column(name = "bus_core_agent_id")
    private String busCoreAgentId; // Agent ID in the bus core system

    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "last_authentication_date")
    private LocalDateTime lastAuthenticationDate;

    @Column(name = "last_booking_date")
    private LocalDateTime lastBookingDate;

    @PrePersist
    public void ensureUid() {
        if (uid == null) {
            uid = new ULID().nextULID();
        }
    }

    // Helper methods
    public boolean isCredentialsValid() {
        return agentLoginName != null && !agentLoginName.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               isActive;
    }

    public boolean isActiveInBusCore() {
        return isActive && "ACTIVE".equalsIgnoreCase(agentStatusInBusCore);
    }

    /**
     * Set encrypted password (used by service layer)
     * 
     * @param encryptedPassword the encrypted password to store
     */
    public void setEncryptedPassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }

    /**
     * Set encrypted transaction password (used by service layer)
     * 
     * @param encryptedTxnPassword the encrypted transaction password to store
     */
    public void setEncryptedTxnPassword(String encryptedTxnPassword) {
        this.txnPassword = encryptedTxnPassword;
    }

    /**
     * Get encrypted password (used by service layer)
     * 
     * @return the encrypted password
     */
    public String getEncryptedPassword() {
        return this.password;
    }

    /**
     * Get encrypted transaction password (used by service layer)
     * 
     * @return the encrypted transaction password
     */
    public String getEncryptedTxnPassword() {
        return this.txnPassword;
    }
}

