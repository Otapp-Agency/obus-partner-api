package com.obuspartners.modules.partner_management.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import de.huxhorn.sulky.ulid.ULID;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.obuspartners.modules.common.domain.entity.BaseEntity;
import com.obuspartners.modules.partner_management.domain.enums.PartnerStatus;
import com.obuspartners.modules.partner_management.domain.enums.PartnerTier;
import com.obuspartners.modules.partner_management.domain.enums.PartnerType;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;

/**
 * Partner entity representing a business partner in the OBUS system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
@Entity
@Table(name = "partners")
public class Partner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(name = "business_name", nullable = false)
    private String businessName;

    @NotBlank
    @Size(max = 200)
    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(max = 20)
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @NotBlank
    @Size(max = 50)
    @Column(name = "business_registration_number", nullable = false)
    private String businessRegistrationNumber;

    @NotBlank
    @Size(max = 50)
    @Column(name = "tax_identification_number", nullable = false)
    private String taxIdentificationNumber;

    @NotBlank
    @Size(max = 500)
    @Column(name = "business_address", nullable = false)
    private String businessAddress;

    @NotBlank
    @Size(max = 100)
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank
    @Size(max = 100)
    @Column(name = "state", nullable = false)
    private String state;

    @NotBlank
    @Size(max = 100)
    @Column(name = "country", nullable = false)
    private String country;

    @NotBlank
    @Size(max = 20)
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PartnerStatus status = PartnerStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PartnerType type = PartnerType.INDIVIDUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private PartnerTier tier = PartnerTier.BRONZE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate = 0.0;

    @NotBlank
    @Size(max = 100)
    @Column(name = "contact_person_name", nullable = false)
    private String contactPersonName;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "contact_person_email", nullable = false)
    private String contactPersonEmail;

    @NotBlank
    @Size(max = 20)
    @Column(name = "contact_person_phone", nullable = false)
    private String contactPersonPhone;

    @Size(max = 1000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 1000)
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // API Keys relationship
    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private java.util.List<PartnerApiKey> apiKeys = new java.util.ArrayList<>();

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
