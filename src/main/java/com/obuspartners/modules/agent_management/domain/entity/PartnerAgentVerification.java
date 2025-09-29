package com.obuspartners.modules.agent_management.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import de.huxhorn.sulky.ulid.ULID;
import com.obuspartners.modules.agent_management.domain.enums.AgentVerificationStatus;
import com.obuspartners.modules.agent_management.service.AgentVerificationService.VerificationStatus;
import com.obuspartners.modules.partner_management.domain.entity.Partner;

import java.time.LocalDateTime;

/**
 * Partner Agent Verification entity for managing agent verification requests
 * Represents a verification request for an agent by a partner
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "partner_agent_verifications", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"partner_id", "agent_id"}),
           @UniqueConstraint(columnNames = {"request_reference_number"})
       })
public class PartnerAgentVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;

    // Partner who initiated the verification request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    @NotNull(message = "Partner is required")
    private Partner partner;

    // Agent being verified
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    @NotNull(message = "Agent is required")
    private Agent agent;

    @Column(name = "request_reference_number", nullable = false)
    @NotBlank(message = "Request reference number is required")
    @Size(max = 50, message = "Request reference number must not exceed 50 characters")
    private String requestReferenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_verification_status", nullable = false)
    @NotNull(message = "Agent Verification status is required")
    private AgentVerificationStatus agentVerificationStatus = AgentVerificationStatus.PENDING;

    @Column(name = "verification_type", length = 50)
    @Size(max = 50, message = "Verification type must not exceed 50 characters")
    private String verificationType; // e.g., "DOCUMENT_VERIFICATION", "IDENTITY_VERIFICATION"

    @Column(name = "requested_by", length = 100)
    @Size(max = 100, message = "Requested by must not exceed 100 characters")
    private String requestedBy; // User who initiated the request

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "verified_by", length = 100)
    @Size(max = 100, message = "Verified by must not exceed 100 characters")
    private String verifiedBy; // User who verified the request

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_notes", length = 1000)
    @Size(max = 1000, message = "Verification notes must not exceed 1000 characters")
    private String verificationNotes;

    @Column(name = "rejection_reason", length = 500)
    @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
    private String rejectionReason;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Optional expiration date for the verification request

    @Column(name = "priority", length = 20)
    @Size(max = 20, message = "Priority must not exceed 20 characters")
    private String priority = "NORMAL"; // HIGH, NORMAL, LOW

    @Column(name = "external_reference", length = 100)
    @Size(max = 100, message = "External reference must not exceed 100 characters")
    private String externalReference; // Reference from external system

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isPending() {
        return agentVerificationStatus == AgentVerificationStatus.PENDING;
    }

    public boolean isApproved() {
        return agentVerificationStatus == AgentVerificationStatus.APPROVED;
    }

    public boolean isRejected() {
        return agentVerificationStatus == AgentVerificationStatus.REJECTED;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void approve(String verifiedBy, String verificationNotes) {
        this.agentVerificationStatus = AgentVerificationStatus.APPROVED;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = LocalDateTime.now();
        this.verificationNotes = verificationNotes;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String verifiedBy, String rejectionReason) {
        this.agentVerificationStatus = AgentVerificationStatus.REJECTED;
        this.verifiedBy = verifiedBy;
        this.verifiedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String cancelledBy) {
        this.agentVerificationStatus = AgentVerificationStatus.CANCELLED;
        this.verifiedBy = cancelledBy;
        this.verifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void setHighPriority() {
        this.priority = "HIGH";
        this.updatedAt = LocalDateTime.now();
    }

    public void setNormalPriority() {
        this.priority = "NORMAL";
        this.updatedAt = LocalDateTime.now();
    }

    public void setLowPriority() {
        this.priority = "LOW";
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (uid == null) {
            uid = new ULID().nextULID();
        }
        if (requestReferenceNumber == null) {
            requestReferenceNumber = new ULID().nextULID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }
}
