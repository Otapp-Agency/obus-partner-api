package com.obuspartners.modules.user_and_role_management.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserStatus;
import de.huxhorn.sulky.ulid.ULID;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SystemUser entity for administrative and management users
 * Represents system users (ADMIN_USER and PARTNER_USER types) with personal and work information
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_users")
public class SystemUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", unique = true, nullable = false)
    @Size(max = 50, message = "UID must not exceed 50 characters")
    private String uid;

    // Bidirectional reference to User entity
    @OneToOne(mappedBy = "systemUser", fetch = FetchType.LAZY)
    private User user;

    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Column(name = "display_name")
    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    @Column(name = "phone_number")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Column(name = "personal_email")
    @Email(message = "Personal email must be valid")
    @Size(max = 100, message = "Personal email must not exceed 100 characters")
    private String personalEmail;

    @Column(name = "employee_id")
    @Size(max = 50, message = "Employee ID must not exceed 50 characters")
    private String employeeId;

    @Column(name = "department")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Column(name = "position")
    @Size(max = 100, message = "Position must not exceed 100 characters")
    private String position;

    @Column(name = "office_location")
    @Size(max = 200, message = "Office location must not exceed 200 characters")
    private String officeLocation;

    @Column(name = "work_phone")
    @Size(max = 20, message = "Work phone must not exceed 20 characters")
    private String workPhone;

    @Column(name = "work_email")
    @Email(message = "Work email must be valid")
    @Size(max = 100, message = "Work email must not exceed 100 characters")
    private String workEmail;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    @Size(max = 20, message = "Gender must not exceed 20 characters")
    private String gender;

    @Column(name = "address", length = 500)
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Column(name = "city")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Column(name = "state")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Column(name = "country")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Column(name = "postal_code")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Column(name = "national_id")
    @Size(max = 50, message = "National ID must not exceed 50 characters")
    private String nationalId;

    @Column(name = "passport_number")
    @Size(max = 50, message = "Passport number must not exceed 50 characters")
    private String passportNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "User status is required")
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "preferred_language")
    @Size(max = 10, message = "Preferred language must not exceed 10 characters")
    private String preferredLanguage = "en";

    @Column(name = "timezone")
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    @Column(name = "profile_picture_url")
    @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
    private String profilePictureUrl;

    @Column(name = "emergency_contact_name")
    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    @Size(max = 20, message = "Emergency contact phone must not exceed 20 characters")
    private String emergencyContactPhone;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "password_changed_date")
    private LocalDateTime passwordChangedDate;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;


    @PrePersist
    public void prePersist() {
        if (this.uid == null || this.uid.isEmpty()) {
            ULID ulid = new ULID();
            this.uid = ulid.nextULID();
        }
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
