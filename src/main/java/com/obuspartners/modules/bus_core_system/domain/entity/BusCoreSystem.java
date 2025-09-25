package com.obuspartners.modules.bus_core_system.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import de.huxhorn.sulky.ulid.ULID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.obuspartners.modules.common.domain.entity.BaseEntity;

import com.obuspartners.modules.user_and_role_management.domain.entity.User;



/**
 * Entity representing a registered bus core system (BMS).
 * 
 * @author OBUS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
@Entity
@Table(name = "bus_core_systems")
public class BusCoreSystem extends BaseEntity {

    // === Primary Keys ===
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;
    
    // === Basic Information ===
    @NotBlank(message = "System code is required")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;
    
    @NotBlank(message = "System name is required")
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @NotBlank(message = "System provider is required")
    @Column(name = "provider_name", nullable = false)
    private String providerName;
    
    @NotBlank(message = "Base URL is required")
    @Column(name = "base_url", nullable = false)
    private String baseUrl;
    
    @Column(name = "description")
    private String description;
    
    // === Configuration ===
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // === User Tracking ===
    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdBy;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by", nullable = false, updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User updatedBy;
    
    @PrePersist
    public void ensureUid() {
        if (uid == null) {
            uid = new ULID().nextULID();
        }
    }
}
