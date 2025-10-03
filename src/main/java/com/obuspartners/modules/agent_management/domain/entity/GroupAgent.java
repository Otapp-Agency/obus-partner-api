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
import com.obuspartners.modules.agent_management.domain.enums.GroupAgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.GroupAgentType;
import com.obuspartners.modules.partner_management.domain.entity.Partner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * GroupAgent entity for external system integration
 * Represents a group of agents that share external system credentials
 * External systems see only the GroupAgent, not individual agents
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "group_agents", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"partner_id", "code"}),
           @UniqueConstraint(columnNames = {"partner_id", "externalSystemIdentifier"})
       })
public class GroupAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;

    // GroupAgent belongs to a partner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    @NotNull(message = "Partner is required")
    private Partner partner;

    @Column(name = "code", nullable = false)
    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Column(name = "description")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    // External system identifier - how this group is known in external systems
    @Column(name = "external_system_identifier", nullable = false)
    @NotBlank(message = "External system identifier is required")
    @Size(max = 100, message = "External system identifier must not exceed 100 characters")
    private String externalSystemIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @NotNull(message = "Group agent type is required")
    private GroupAgentType type = GroupAgentType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Group agent status is required")
    private GroupAgentStatus status = GroupAgentStatus.ACTIVE;

    // Contact information for external system integration
    @Column(name = "contact_person")
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @Column(name = "contact_email")
    @Email(message = "Contact email must be valid")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @Column(name = "contact_phone")
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    // Business information
    @Column(name = "business_name")
    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String businessName;

    @Column(name = "business_address", length = 500)
    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;

    @Column(name = "tax_id")
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @Column(name = "license_number")
    @Size(max = 50, message = "License number must not exceed 50 characters")
    private String licenseNumber;

    // Timestamps
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // One-to-many relationship with agents
    @OneToMany(mappedBy = "groupAgent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Agent> agents = new ArrayList<>();

    // One-to-many relationship with bus core system credentials
    @OneToMany(mappedBy = "groupAgent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupAgentCoreBusSystem> coreBusSystems = new ArrayList<>();

    // Business methods
    public boolean isActive() {
        return status == GroupAgentStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return status == GroupAgentStatus.SUSPENDED;
    }

    public boolean isInactive() {
        return status == GroupAgentStatus.INACTIVE;
    }

    public void activate() {
        this.status = GroupAgentStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = GroupAgentStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = GroupAgentStatus.INACTIVE;
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

    // Agent management methods
    public void addAgent(Agent agent) {
        if (!agents.contains(agent)) {
            agents.add(agent);
            agent.setGroupAgent(this);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeAgent(Agent agent) {
        if (agents.remove(agent)) {
            agent.setGroupAgent(null);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public int getAgentCount() {
        return agents.size();
    }

    public boolean hasAgents() {
        return !agents.isEmpty();
    }

    // Bus core system management methods
    public void addCoreBusSystem(GroupAgentCoreBusSystem coreBusSystem) {
        if (!coreBusSystems.contains(coreBusSystem)) {
            coreBusSystems.add(coreBusSystem);
            coreBusSystem.setGroupAgent(this);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeCoreBusSystem(GroupAgentCoreBusSystem coreBusSystem) {
        if (coreBusSystems.remove(coreBusSystem)) {
            coreBusSystem.setGroupAgent(null);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public boolean hasCoreBusSystems() {
        return !coreBusSystems.isEmpty();
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
