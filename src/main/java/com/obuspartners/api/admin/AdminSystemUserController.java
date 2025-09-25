package com.obuspartners.api.admin;

import com.obuspartners.modules.common.util.PageResponseWrapper;
import com.obuspartners.modules.common.util.ResponseWrapper;
import com.obuspartners.modules.user_and_role_management.domain.dto.CreatePartnerUserRequestDto;
import com.obuspartners.modules.user_and_role_management.domain.dto.CreateAdminUserRequestDto;
import com.obuspartners.modules.user_and_role_management.domain.dto.SystemUserResponseDto;
import com.obuspartners.modules.user_and_role_management.domain.dto.UpdateSystemUserRequestDto;
import com.obuspartners.modules.user_and_role_management.domain.entity.SystemUser;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserStatus;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserType;
import com.obuspartners.modules.user_and_role_management.service.SystemUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/admin/v1/system-users")
@RequiredArgsConstructor
@Tag(name = "Admin System User Management", description = "Administrative endpoints for managing system users")
public class AdminSystemUserController {

    private final SystemUserService systemUserService;

    @GetMapping
    @Operation(summary = "Get all system users", description = "Retrieves a paginated list of all system users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<PageResponseWrapper<SystemUserResponseDto>> getAllSystemUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Fetching system users - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<SystemUser> systemUsers = systemUserService.getAllSystemUsers(pageable);
        Page<SystemUserResponseDto> responseDtos = systemUsers.map(this::convertToResponseDto);
        
        log.info("Retrieved {} system users out of {} total", responseDtos.getNumberOfElements(), systemUsers.getTotalElements());
        return ResponseEntity.ok(PageResponseWrapper.fromPage(responseDtos, "System users retrieved successfully"));
    }

    @PostMapping("/partner-users")
    @Operation(summary = "Create a new partner user", description = "Creates a new partner user with generated password and sends welcome email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Partner user created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<SystemUserResponseDto> createPartnerUser(
            @Valid @RequestBody CreatePartnerUserRequestDto request) {
        log.info("Creating partner user for username: {}", request.getUsername());
        
        SystemUser systemUser = systemUserService.createPartnerUser(request);
        SystemUserResponseDto response = convertToResponseDto(systemUser);
        
        log.info("Partner user created successfully with UID: {}", systemUser.getUid());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin-users")
    @Operation(summary = "Create a new admin user", description = "Creates a new admin user with generated password and sends welcome email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin user created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<SystemUserResponseDto> createAdminUser(
            @Valid @RequestBody CreateAdminUserRequestDto request) {
        log.info("Creating admin user for username: {}", request.getUsername());
        
        SystemUser systemUser = systemUserService.createAdminUser(request);
        SystemUserResponseDto response = convertToResponseDto(systemUser);
        
        log.info("Admin user created successfully with UID: {}", systemUser.getUid());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/uid/{uid}")
    @Operation(summary = "Get system user by UID", description = "Retrieves a system user by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System user found"),
            @ApiResponse(responseCode = "404", description = "System user not found")
    })
    public ResponseEntity<ResponseWrapper<SystemUserResponseDto>> getSystemUserByUid(
            @Parameter(description = "System user UID") @PathVariable String uid) {
        log.info("Retrieving system user with UID: {}", uid);
        
        Optional<SystemUser> systemUser = systemUserService.findByUid(uid);
        if (systemUser.isEmpty()) {
            log.warn("System user not found with UID: {}", uid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseWrapper<>(false, 404, "System user not found with UID: " + uid, null));
        }
        
        SystemUserResponseDto response = convertToResponseDto(systemUser.get());
        log.info("System user retrieved successfully with UID: {}", uid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "System user retrieved successfully", response));
    }

    @PutMapping("/uid/{uid}")
    @Operation(summary = "Update system user by UID", description = "Updates a system user's information by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System user updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "System user not found")
    })
    public ResponseEntity<ResponseWrapper<SystemUserResponseDto>> updateSystemUserByUid(
            @Parameter(description = "System user UID") @PathVariable String uid,
            @Valid @RequestBody UpdateSystemUserRequestDto request) {
        log.info("Updating system user with UID: {}", uid);
        
        SystemUser systemUser = systemUserService.updateSystemUserByUid(uid, request);
        SystemUserResponseDto response = convertToResponseDto(systemUser);
        
        log.info("System user updated successfully with UID: {}", uid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "System user updated successfully", response));
    }

    @DeleteMapping("/uid/{uid}")
    @Operation(summary = "Delete system user by UID", description = "Deletes a system user by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "System user deleted successfully"),
            @ApiResponse(responseCode = "404", description = "System user not found")
    })
    public ResponseEntity<Void> deleteSystemUserByUid(
            @Parameter(description = "System user UID") @PathVariable String uid) {
        log.info("Deleting system user with UID: {}", uid);
        
        systemUserService.deleteByUid(uid);
        
        log.info("System user deleted successfully with UID: {}", uid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/by-user-type")
    @Operation(summary = "Get system users by user type", description = "Retrieves system users filtered by user type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System users retrieved successfully")
    })
    public ResponseEntity<List<SystemUserResponseDto>> getSystemUsersByUserType(
            @Parameter(description = "User type filter") @RequestParam UserType userType) {
        log.info("Retrieving system users by user type: {}", userType);
        
        List<SystemUser> systemUsers = systemUserService.findByUserType(userType);
        List<SystemUserResponseDto> response = systemUsers.stream()
                .map(this::convertToResponseDto)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/by-status")
    @Operation(summary = "Get system users by status", description = "Retrieves system users filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System users retrieved successfully")
    })
    public ResponseEntity<List<SystemUserResponseDto>> getSystemUsersByStatus(
            @Parameter(description = "Status filter") @RequestParam UserStatus status) {
        log.info("Retrieving system users by status: {}", status);
        
        List<SystemUser> systemUsers = systemUserService.findByStatus(status);
        List<SystemUserResponseDto> response = systemUsers.stream()
                .map(this::convertToResponseDto)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/by-department")
    @Operation(summary = "Get system users by department", description = "Retrieves system users filtered by department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System users retrieved successfully")
    })
    public ResponseEntity<List<SystemUserResponseDto>> getSystemUsersByDepartment(
            @Parameter(description = "Department filter") @RequestParam String department) {
        log.info("Retrieving system users by department: {}", department);
        
        List<SystemUser> systemUsers = systemUserService.findByDepartment(department);
        List<SystemUserResponseDto> response = systemUsers.stream()
                .map(this::convertToResponseDto)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/uid/{uid}/activate")
    @Operation(summary = "Activate system user", description = "Activates a system user by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System user activated successfully"),
            @ApiResponse(responseCode = "404", description = "System user not found")
    })
    public ResponseEntity<SystemUserResponseDto> activateSystemUser(
            @Parameter(description = "System user UID") @PathVariable String uid) {
        log.info("Activating system user with UID: {}", uid);
        
        SystemUser systemUser = systemUserService.activateByUid(uid);
        SystemUserResponseDto response = convertToResponseDto(systemUser);
        
        log.info("System user activated successfully with UID: {}", uid);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/uid/{uid}/deactivate")
    @Operation(summary = "Deactivate system user", description = "Deactivates a system user by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System user deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "System user not found")
    })
    public ResponseEntity<SystemUserResponseDto> deactivateSystemUser(
            @Parameter(description = "System user UID") @PathVariable String uid) {
        log.info("Deactivating system user with UID: {}", uid);
        
        SystemUser systemUser = systemUserService.deactivateByUid(uid);
        SystemUserResponseDto response = convertToResponseDto(systemUser);
        
        log.info("System user deactivated successfully with UID: {}", uid);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/uid/{uid}/suspend")
    @Operation(summary = "Suspend system user", description = "Suspends a system user by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System user suspended successfully"),
            @ApiResponse(responseCode = "404", description = "System user not found")
    })
    public ResponseEntity<SystemUserResponseDto> suspendSystemUser(
            @Parameter(description = "System user UID") @PathVariable String uid) {
        log.info("Suspending system user with UID: {}", uid);
        
        SystemUser systemUser = systemUserService.suspendByUid(uid);
        SystemUserResponseDto response = convertToResponseDto(systemUser);
        
        log.info("System user suspended successfully with UID: {}", uid);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/uid/{uid}/verify-email")
    @Operation(summary = "Verify system user email", description = "Marks a system user's email as verified")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "404", description = "System user not found")
    })
    public ResponseEntity<SystemUserResponseDto> verifyEmail(
            @Parameter(description = "System user UID") @PathVariable String uid) {
        log.info("Verifying email for system user with UID: {}", uid);
        
        SystemUser systemUser = systemUserService.verifyEmailByUid(uid);
        SystemUserResponseDto response = convertToResponseDto(systemUser);
        
        log.info("Email verified successfully for system user with UID: {}", uid);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/uid/{uid}/verify-phone")
    @Operation(summary = "Verify system user phone", description = "Marks a system user's phone as verified")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phone verified successfully"),
            @ApiResponse(responseCode = "404", description = "System user not found")
    })
    public ResponseEntity<SystemUserResponseDto> verifyPhone(
            @Parameter(description = "System user UID") @PathVariable String uid) {
        log.info("Verifying phone for system user with UID: {}", uid);
        
        SystemUser systemUser = systemUserService.verifyPhoneByUid(uid);
        SystemUserResponseDto response = convertToResponseDto(systemUser);
        
        log.info("Phone verified successfully for system user with UID: {}", uid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/count-by-user-type")
    @Operation(summary = "Get count by user type", description = "Returns the count of system users grouped by user type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Long> getCountByUserType(
            @Parameter(description = "User type") @RequestParam UserType userType) {
        log.info("Getting count of system users by user type: {}", userType);
        
        long count = systemUserService.countByUserType(userType);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/count-by-status")
    @Operation(summary = "Get count by status", description = "Returns the count of system users grouped by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Long> getCountByStatus(
            @Parameter(description = "Status") @RequestParam UserStatus status) {
        log.info("Getting count of system users by status: {}", status);
        
        long count = systemUserService.countByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/count-by-department")
    @Operation(summary = "Get count by department", description = "Returns the count of system users grouped by department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Long> getCountByDepartment(
            @Parameter(description = "Department") @RequestParam String department) {
        log.info("Getting count of system users by department: {}", department);
        
        long count = systemUserService.countByDepartment(department);
        return ResponseEntity.ok(count);
    }

    /**
     * Converts SystemUser entity to SystemUserResponseDto
     * Includes both SystemUser and User fields
     */
    private SystemUserResponseDto convertToResponseDto(SystemUser systemUser) {
        return SystemUserResponseDto.builder()
                // SystemUser fields
                .id(systemUser.getId())
                .uid(systemUser.getUid())
                .firstName(systemUser.getFirstName())
                .lastName(systemUser.getLastName())
                .systemUserDisplayName(systemUser.getDisplayName())
                .phoneNumber(systemUser.getPhoneNumber())
                .personalEmail(systemUser.getPersonalEmail())
                .employeeId(systemUser.getEmployeeId())
                .department(systemUser.getDepartment())
                .position(systemUser.getPosition())
                .officeLocation(systemUser.getOfficeLocation())
                .workPhone(systemUser.getWorkPhone())
                .workEmail(systemUser.getWorkEmail())
                .address(systemUser.getAddress())
                .city(systemUser.getCity())
                .state(systemUser.getState())
                .country(systemUser.getCountry())
                .postalCode(systemUser.getPostalCode())
                .nationalId(systemUser.getNationalId())
                .passportNumber(systemUser.getPassportNumber())
                .gender(systemUser.getGender())
                .preferredLanguage(systemUser.getPreferredLanguage())
                .timezone(systemUser.getTimezone())
                .emergencyContactName(systemUser.getEmergencyContactName())
                .emergencyContactPhone(systemUser.getEmergencyContactPhone())
                .status(systemUser.getStatus())
                .emailVerified(systemUser.getEmailVerified())
                .phoneVerified(systemUser.getPhoneVerified())
                .registrationDate(systemUser.getRegistrationDate())
                .lastLoginDate(systemUser.getLastLoginDate())
                .createdAt(systemUser.getCreatedAt())
                .updatedAt(systemUser.getUpdatedAt())
                .createdBy(systemUser.getCreatedBy())
                .updatedBy(systemUser.getUpdatedBy())
                // User fields (with null check)
                .userId(systemUser.getUser() != null ? systemUser.getUser().getId() : null)
                .username(systemUser.getUser() != null ? systemUser.getUser().getUsername() : null)
                .email(systemUser.getUser() != null ? systemUser.getUser().getEmail() : null)
                .displayName(systemUser.getUser() != null ? systemUser.getUser().getDisplayName() : null)
                .userType(systemUser.getUser() != null ? systemUser.getUser().getUserType() : null)
                .enabled(systemUser.getUser() != null ? systemUser.getUser().getEnabled() : null)
                .accountNonExpired(systemUser.getUser() != null ? systemUser.getUser().getAccountNonExpired() : null)
                .accountNonLocked(systemUser.getUser() != null ? systemUser.getUser().getAccountNonLocked() : null)
                .credentialsNonExpired(systemUser.getUser() != null ? systemUser.getUser().getCredentialsNonExpired() : null)
                .requirePasswordChange(systemUser.getUser() != null ? systemUser.getUser().getRequirePasswordChange() : null)
                // Partner information
                .partnerId(systemUser.getUser() != null && systemUser.getUser().getPartner() != null ? systemUser.getUser().getPartner().getId() : null)
                .partnerName(systemUser.getUser() != null && systemUser.getUser().getPartner() != null ? systemUser.getUser().getPartner().getBusinessName() : null)
                .partnerCode(systemUser.getUser() != null && systemUser.getUser().getPartner() != null ? systemUser.getUser().getPartner().getCode() : null)
                .build();
    }
}
