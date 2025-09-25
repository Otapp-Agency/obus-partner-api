package com.obuspartners.modules.user_and_role_management.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserStatus;

/**
 * DTO for updating system user information
 * Contains fields that can be updated for SystemUser entity
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSystemUserRequestDto {

    // SystemUser UID
    @Size(max = 50, message = "UID must not exceed 50 characters")
    private String uid;

    // Personal information
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Email(message = "Personal email must be valid")
    @Size(max = 100, message = "Personal email must not exceed 100 characters")
    private String personalEmail;

    // Work information
    @Size(max = 50, message = "Employee ID must not exceed 50 characters")
    private String employeeId;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Size(max = 100, message = "Position must not exceed 100 characters")
    private String position;

    @Size(max = 200, message = "Office location must not exceed 200 characters")
    private String officeLocation;

    @Size(max = 20, message = "Work phone must not exceed 20 characters")
    private String workPhone;

    @Email(message = "Work email must be valid")
    @Size(max = 100, message = "Work email must not exceed 100 characters")
    private String workEmail;

    // Address information
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    // Additional information
    @Size(max = 50, message = "National ID must not exceed 50 characters")
    private String nationalId;

    @Size(max = 50, message = "Passport number must not exceed 50 characters")
    private String passportNumber;

    @Size(max = 10, message = "Gender must not exceed 10 characters")
    private String gender;

    @Size(max = 10, message = "Preferred language must not exceed 10 characters")
    private String preferredLanguage;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
    private String profilePictureUrl;

    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;

    @Size(max = 20, message = "Emergency contact phone must not exceed 20 characters")
    private String emergencyContactPhone;

    // Status
    private UserStatus status;

}
