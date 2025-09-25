package com.obuspartners.modules.agent_management.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;

import java.time.LocalDateTime;
import java.math.BigDecimal;

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
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bidirectional reference to User entity
    @OneToOne(mappedBy = "agent", fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "agent_code", unique = true, nullable = false)
    @NotBlank(message = "Agent code is required")
    @Size(max = 20, message = "Agent code must not exceed 20 characters")
    private String agentCode;

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

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

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

    // Custom constructors
    public Agent(String agentCode, AgentType agentType) {
        this.agentCode = agentCode;
        this.agentType = agentType;
        this.status = AgentStatus.PENDING_APPROVAL;
        this.currentBalance = BigDecimal.ZERO;
        this.registrationDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Agent(String agentCode, String businessName, AgentType agentType) {
        this(agentCode, agentType);
        this.businessName = businessName;
    }

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

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
