package com.obuspartners.modules.agent_management.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import de.huxhorn.sulky.ulid.ULID;
import com.obuspartners.modules.agent_management.domain.enums.AgentRequestStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;
import com.obuspartners.modules.partner_management.domain.entity.Partner;

import java.time.LocalDateTime;

/**
 * Agent Request Entity
 * Temporary entity for storing agent registration requests before verification
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "agent_requests",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"partner_id", "partnerAgentNumber"}),
           @UniqueConstraint(columnNames = {"partner_id", "msisdn"})
       })
public class AgentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    @NotNull(message = "Partner is required")
    private Partner partner;

    @Column(name = "partner_agent_number", nullable = false, length = 50)
    @NotBlank(message = "Partner agent number is required")
    @Size(max = 50, message = "Partner agent number must not exceed 50 characters")
    private String partnerAgentNumber;

    @Column(name = "business_name", nullable = false, length = 200)
    @NotBlank(message = "Business name is required")
    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String businessName;

    @Column(name = "contact_person", nullable = false, length = 100)
    @NotBlank(message = "Contact person is required")
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @Column(name = "phone_number", length = 20)
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Column(name = "msisdn", length = 20)
    @Size(max = 20, message = "MSISDN must not exceed 20 characters")
    private String msisdn;

    @Column(name = "business_email", length = 100)
    @Email(message = "Business email must be valid")
    @Size(max = 100, message = "Business email must not exceed 100 characters")
    private String businessEmail;

    @Column(name = "business_address", length = 500)
    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;

    @Column(name = "tax_id", length = 50)
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @Column(name = "license_number", length = 100)
    @Size(max = 100, message = "License number must not exceed 100 characters")
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false)
    @NotNull(message = "Agent type is required")
    private AgentType agentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_agent_id")
    private Agent superAgent;

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Status is required")
    private AgentRequestStatus status = AgentRequestStatus.PENDING;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by", length = 100)
    @Size(max = 100, message = "Processed by must not exceed 100 characters")
    private String processedBy;

    @Column(name = "rejection_reason", length = 500)
    @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
    private String rejectionReason;

    @Column(name = "verification_reference_number", length = 50)
    @Size(max = 50, message = "Verification reference number must not exceed 50 characters")
    private String verificationReferenceNumber;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isPending() {
        return status == AgentRequestStatus.PENDING;
    }

    public boolean isApproved() {
        return status == AgentRequestStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == AgentRequestStatus.REJECTED;
    }

    public boolean isCancelled() {
        return status == AgentRequestStatus.CANCELLED;
    }

    public boolean isExpired() {
        return status == AgentRequestStatus.EXPIRED;
    }

    public boolean isFinal() {
        return status.isFinal();
    }

    public boolean isExpiredByTime() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void approve(String processedBy) {
        this.status = AgentRequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String processedBy, String rejectionReason) {
        this.status = AgentRequestStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
        this.rejectionReason = rejectionReason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String processedBy) {
        this.status = AgentRequestStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = AgentRequestStatus.EXPIRED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = "SYSTEM";
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (uid == null) {
            uid = new ULID().nextULID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(30); // 30 days expiry
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
