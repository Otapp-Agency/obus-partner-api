package com.obuspartners.modules.user_and_role_management.service;

import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

/**
 * User service interface
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface UserService extends UserDetailsService {

    /**
     * Save a new user
     * 
     * @param user the user to save
     * @return the saved user
     */
    User save(User user);

    /**
     * Update an existing user
     * 
     * @param user the user to update
     * @return the updated user
     */
    User update(User user);

    /**
     * Find user by username
     * 
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * 
     * @param email the email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users
     * 
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Find user by ID
     * 
     * @param id the user ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(Long id);

    /**
     * Check if username exists
     * 
     * @param username the username
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * 
     * @param email the email
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Delete user by ID
     * 
     * @param id the user ID
     */
    void deleteById(Long id);

    /**
     * Enable or disable user account
     * 
     * @param id the user ID
     * @param enabled true to enable, false to disable
     * @return the updated user
     */
    User setUserEnabled(Long id, boolean enabled);

    /**
     * Add role to user
     * 
     * @param userId the user ID
     * @param roleId the role ID
     * @return the updated user
     */
    User addRoleToUser(Long userId, Long roleId);

    /**
     * Remove role from user
     * 
     * @param userId the user ID
     * @param roleId the role ID
     * @return the updated user
     */
    User removeRoleFromUser(Long userId, Long roleId);

    /**
     * Set password change requirement for user
     * 
     * @param userId the user ID
     * @param requirePasswordChange true to require password change, false otherwise
     * @return the updated user
     */
    User setPasswordChangeRequired(Long userId, boolean requirePasswordChange);

    /**
     * Change user password
     * 
     * @param userId the user ID
     * @param newPassword the new password
     * @return the updated user
     */
    User changePassword(Long userId, String newPassword);

    /**
     * Check if user requires password change
     * 
     * @param userId the user ID
     * @return true if password change is required, false otherwise
     */
    boolean isPasswordChangeRequired(Long userId);
}