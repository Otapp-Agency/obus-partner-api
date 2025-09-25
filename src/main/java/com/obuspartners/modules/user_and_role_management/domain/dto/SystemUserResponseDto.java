package com.obuspartners.modules.user_and_role_management.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserStatus;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for SystemUser response data
 * Contains information to be returned in API responses
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemUserResponseDto {

    // SystemUser ID
    private Long id;

    // SystemUser UID
    private String uid;

    // User account information
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private UserType userType;
    private Boolean enabled;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private Boolean requirePasswordChange;

    // Partner information (for PARTNER_USER type)
    private Long partnerId;
    private String partnerName;
    private String partnerCode;

    // Personal information
    private String firstName;
    private String lastName;
    private String systemUserDisplayName;
    private String phoneNumber;
    private String personalEmail;
    private LocalDate dateOfBirth;
    private String gender;

    // Work information
    private String employeeId;
    private String department;
    private String position;
    private String officeLocation;
    private String workPhone;
    private String workEmail;

    // Address information
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    // Additional information
    private String nationalId;
    private String passportNumber;
    private UserStatus status;
    private String preferredLanguage;
    private String timezone;
    private String profilePictureUrl;

    // Emergency contact
    private String emergencyContactName;
    private String emergencyContactPhone;

    // Timestamps
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;
    private LocalDateTime passwordChangedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Verification status
    private Boolean emailVerified;
    private Boolean phoneVerified;

    // Computed fields
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isVerified() {
        return emailVerified && phoneVerified;
    }
}
