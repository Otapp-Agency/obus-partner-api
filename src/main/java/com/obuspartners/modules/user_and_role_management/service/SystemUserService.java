package com.obuspartners.modules.user_and_role_management.service;

import com.obuspartners.modules.user_and_role_management.domain.entity.SystemUser;
import com.obuspartners.modules.user_and_role_management.domain.dto.CreatePartnerUserRequestDto;
import com.obuspartners.modules.user_and_role_management.domain.dto.CreateAdminUserRequestDto;
import com.obuspartners.modules.user_and_role_management.domain.dto.UpdateSystemUserRequestDto;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserStatus;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for SystemUser management
 * Handles business logic for system users (ADMIN_USER and PARTNER_USER types)
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface SystemUserService {

    /**
     * Create a new partner user
     * Creates both User and SystemUser entities for PARTNER_USER type
     * A strong password will be automatically generated for the user account
     * 
     * @param request the request containing all necessary information to create User and SystemUser for PARTNER_USER
     * @return the created system user
     */
    SystemUser createPartnerUser(CreatePartnerUserRequestDto request);

    /**
     * Create a new admin user
     * Creates both User and SystemUser entities for ADMIN_USER type
     * A strong password will be automatically generated for the user account
     * 
     * @param request the request containing all necessary information to create User and SystemUser for ADMIN_USER
     * @return the created system user
     */
    SystemUser createAdminUser(CreateAdminUserRequestDto request);

    /**
     * Find system user by ID
     * 
     * @param id the system user ID
     * @return Optional containing the system user if found
     */
    Optional<SystemUser> findById(Long id);

    /**
     * Find system user by username
     * 
     * @param username the username
     * @return Optional containing the system user if found
     */
    Optional<SystemUser> findByUsername(String username);

    /**
     * Find system user by user ID
     * 
     * @param userId the user ID
     * @return Optional containing the system user if found
     */
    Optional<SystemUser> findByUserId(Long userId);

    /**
     * Find system user by UID
     * 
     * @param uid the UID
     * @return Optional containing the system user if found
     */
    Optional<SystemUser> findByUid(String uid);

    /**
     * Get all system users with pagination
     * 
     * @param pageable pagination information
     * @return Page of system users
     */
    Page<SystemUser> getAllSystemUsers(Pageable pageable);

    /**
     * Find system user by employee ID
     * 
     * @param employeeId the employee ID
     * @return Optional containing the system user if found
     */
    Optional<SystemUser> findByEmployeeId(String employeeId);

    /**
     * Find system user by email (personal or work email)
     * 
     * @param email the email address
     * @return Optional containing the system user if found
     */
    Optional<SystemUser> findByEmail(String email);

    /**
     * Find system users by user type
     * 
     * @param userType the user type (ADMIN_USER or PARTNER_USER)
     * @return list of system users with the specified user type
     */
    List<SystemUser> findByUserType(UserType userType);

    /**
     * Find system users by status
     * 
     * @param status the user status
     * @return list of system users with the specified status
     */
    List<SystemUser> findByStatus(UserStatus status);

    /**
     * Find system users by department
     * 
     * @param department the department
     * @return list of system users in the specified department
     */
    List<SystemUser> findByDepartment(String department);

    /**
     * Update system user information
     * 
     * @param id the system user ID
     * @param request the request containing updated information
     * @return the updated system user
     */
    SystemUser updateSystemUser(Long id, UpdateSystemUserRequestDto request);

    /**
     * Update system user information by UID
     * 
     * @param uid the system user UID
     * @param request the request containing updated information
     * @return the updated system user
     */
    SystemUser updateSystemUserByUid(String uid, UpdateSystemUserRequestDto request);

    /**
     * Update system user status
     * 
     * @param id the system user ID
     * @param status the new status
     * @return the updated system user
     */
    SystemUser updateStatus(Long id, UserStatus status);

    /**
     * Update system user status by UID
     * 
     * @param uid the system user UID
     * @param status the new status
     * @return the updated system user
     */
    SystemUser updateStatusByUid(String uid, UserStatus status);

    /**
     * Activate system user
     * 
     * @param id the system user ID
     * @return the updated system user
     */
    SystemUser activate(Long id);

    /**
     * Activate system user by UID
     * 
     * @param uid the system user UID
     * @return the updated system user
     */
    SystemUser activateByUid(String uid);

    /**
     * Deactivate system user
     * 
     * @param id the system user ID
     * @return the updated system user
     */
    SystemUser deactivate(Long id);

    /**
     * Deactivate system user by UID
     * 
     * @param uid the system user UID
     * @return the updated system user
     */
    SystemUser deactivateByUid(String uid);

    /**
     * Suspend system user
     * 
     * @param id the system user ID
     * @return the updated system user
     */
    SystemUser suspend(Long id);

    /**
     * Suspend system user by UID
     * 
     * @param uid the system user UID
     * @return the updated system user
     */
    SystemUser suspendByUid(String uid);

    /**
     * Verify email for system user
     * 
     * @param id the system user ID
     * @return the updated system user
     */
    SystemUser verifyEmail(Long id);

    /**
     * Verify email for system user by UID
     * 
     * @param uid the system user UID
     * @return the updated system user
     */
    SystemUser verifyEmailByUid(String uid);

    /**
     * Verify phone for system user
     * 
     * @param id the system user ID
     * @return the updated system user
     */
    SystemUser verifyPhone(Long id);

    /**
     * Verify phone for system user by UID
     * 
     * @param uid the system user UID
     * @return the updated system user
     */
    SystemUser verifyPhoneByUid(String uid);

    /**
     * Update last login date for system user
     * 
     * @param id the system user ID
     * @return the updated system user
     */
    SystemUser updateLastLogin(Long id);

    /**
     * Update last login date for system user by UID
     * 
     * @param uid the system user UID
     * @return the updated system user
     */
    SystemUser updateLastLoginByUid(String uid);

    /**
     * Update password changed date for system user
     * 
     * @param id the system user ID
     * @return the updated system user
     */
    SystemUser updatePasswordChangedDate(Long id);

    /**
     * Update password changed date for system user by UID
     * 
     * @param uid the system user UID
     * @return the updated system user
     */
    SystemUser updatePasswordChangedDateByUid(String uid);

    /**
     * Check if UID exists
     * 
     * @param uid the UID
     * @return true if UID exists, false otherwise
     */
    boolean existsByUid(String uid);

    /**
     * Check if employee ID exists
     * 
     * @param employeeId the employee ID
     * @return true if employee ID exists, false otherwise
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * Check if personal email exists
     * 
     * @param personalEmail the personal email
     * @return true if personal email exists, false otherwise
     */
    boolean existsByPersonalEmail(String personalEmail);

    /**
     * Check if work email exists
     * 
     * @param workEmail the work email
     * @return true if work email exists, false otherwise
     */
    boolean existsByWorkEmail(String workEmail);

    /**
     * Delete system user by ID
     * 
     * @param id the system user ID
     */
    void deleteById(Long id);

    /**
     * Delete system user by UID
     * 
     * @param uid the system user UID
     */
    void deleteByUid(String uid);

    /**
     * Delete system user
     * 
     * @param systemUser the system user to delete
     */
    void delete(SystemUser systemUser);

    /**
     * Count total system users
     * 
     * @return total count of system users
     */
    long count();

    /**
     * Count system users by user type
     * 
     * @param userType the user type
     * @return count of system users with the specified user type
     */
    long countByUserType(UserType userType);

    /**
     * Count system users by status
     * 
     * @param status the user status
     * @return count of system users with the specified status
     */
    long countByStatus(UserStatus status);

    /**
     * Count system users by department
     * 
     * @param department the department
     * @return count of system users in the specified department
     */
    long countByDepartment(String department);
}
