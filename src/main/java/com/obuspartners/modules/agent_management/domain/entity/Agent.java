package com.obuspartners.modules.agent_management.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import de.huxhorn.sulky.ulid.ULID;
import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.partner_management.domain.entity.Partner;

import java.time.LocalDateTime;

/**
 * Agent entity for business operations
 * Represents an agent with business-specific attributes separate from authentication
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "agents", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"partner_id", "partnerAgentNumber"})
       })
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;

    // Bidirectional reference to User entity
    @OneToOne(mappedBy = "agent", fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Agent belongs to a partner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    @NotNull(message = "Partner is required")
    private Partner partner;

    // Super agent relationship for sub-agents
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_agent_id")
    private Agent superAgent;

    @Column(name = "code", unique = true, nullable = false)
    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @Column(name = "partner_agent_number", nullable = false)
    @NotBlank(message = "Partner agent number is required")
    @Size(max = 50, message = "Partner agent number must not exceed 50 characters")
    private String partnerAgentNumber;

    @Column(name = "login_username", unique = true, nullable = false)
    @NotBlank(message = "Login username is required")
    @Size(max = 100, message = "Login username must not exceed 100 characters")
    private String loginUsername;

    @Column(name = "login_password", nullable = false)
    @NotBlank(message = "Login password is required")
    @Size(max = 255, message = "Login password must not exceed 255 characters")
    private String loginPassword;

    @Column(name = "business_name")
    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String businessName;

    @Column(name = "contact_person")
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @Column(name = "phone_number")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Column(name = "business_email")
    @Email(message = "Business email must be valid")
    @Size(max = 100, message = "Business email must not exceed 100 characters")
    private String businessEmail;

    @Column(name = "business_address", length = 500)
    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;

    @Column(name = "tax_id")
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @Column(name = "license_number")
    @Size(max = 50, message = "License number must not exceed 50 characters")
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Agent type is required")
    private AgentType agentType = AgentType.INDIVIDUAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Agent status is required")
    private AgentStatus status = AgentStatus.PENDING_APPROVAL;


    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public boolean isActive() {
        return status == AgentStatus.ACTIVE;
    }

    public boolean isApproved() {
        return status == AgentStatus.ACTIVE || status == AgentStatus.SUSPENDED;
    }

    public void approve() {
        this.status = AgentStatus.ACTIVE;
        this.approvalDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = AgentStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = AgentStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = AgentStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastActivity() {
        this.lastActivityDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Partner relationship methods
    public boolean belongsToPartner(Partner partner) {
        return this.partner != null && this.partner.equals(partner);
    }

    // Super agent relationship methods
    public boolean isSubAgent() {
        return this.agentType == AgentType.SUB_AGENT && this.superAgent != null;
    }

    public boolean isSuperAgent() {
        return this.agentType == AgentType.SUPER_AGENT;
    }

    public boolean hasSubAgents() {
        // This would need to be implemented with a query or collection
        // For now, we'll return false as we don't have the reverse relationship
        return false;
    }

    public void assignSuperAgent(Agent superAgent) {
        if (this.agentType == AgentType.SUB_AGENT) {
            this.superAgent = superAgent;
            this.updatedAt = LocalDateTime.now();
        }
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
