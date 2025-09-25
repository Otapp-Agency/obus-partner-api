package com.obuspartners.modules.user_and_role_management.service;

import com.obuspartners.modules.user_and_role_management.domain.entity.Role;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.repository.RoleRepository;
import com.obuspartners.modules.user_and_role_management.repository.UserRepository;
import com.obuspartners.modules.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User service implementation
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return user;
    }

    @Override
    public User save(User user) {
        log.debug("Saving user: {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        log.info("User saved successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public User update(User user) {
        log.debug("Updating user: {}", user.getUsername());
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return updatedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        log.debug("Checking if username exists: {}", username);
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting user by ID: {}", id);
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public User setUserEnabled(Long id, boolean enabled) {
        log.debug("Setting user enabled status: {} for user ID: {}", enabled, id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found with ID: " + id, HttpStatus.NOT_FOUND));
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        log.info("User enabled status updated to: {} for user ID: {}", enabled, id);
        return updatedUser;
    }

    @Override
    public User addRoleToUser(Long userId, Long roleId) {
        log.debug("Adding role ID: {} to user ID: {}", roleId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found with ID: " + userId, HttpStatus.NOT_FOUND));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException("Role not found with ID: " + roleId, HttpStatus.NOT_FOUND));
        
        user.addRole(role);
        User updatedUser = userRepository.save(user);
        log.info("Role '{}' added to user '{}' successfully", role.getRoleType(), user.getUsername());
        return updatedUser;
    }

    @Override
    public User removeRoleFromUser(Long userId, Long roleId) {
        log.debug("Removing role ID: {} from user ID: {}", roleId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found with ID: " + userId, HttpStatus.NOT_FOUND));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ApiException("Role not found with ID: " + roleId, HttpStatus.NOT_FOUND));
        
        user.removeRole(role);
        User updatedUser = userRepository.save(user);
        log.info("Role '{}' removed from user '{}' successfully", role.getRoleType(), user.getUsername());
        return updatedUser;
    }

    @Override
    public User setPasswordChangeRequired(Long userId, boolean requirePasswordChange) {
        log.debug("Setting password change requirement: {} for user ID: {}", requirePasswordChange, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found with ID: " + userId, HttpStatus.NOT_FOUND));
        user.setRequirePasswordChange(requirePasswordChange);
        User updatedUser = userRepository.save(user);
        log.info("Password change requirement updated to: {} for user ID: {}", requirePasswordChange, userId);
        return updatedUser;
    }

    @Override
    public User changePassword(Long userId, String newPassword) {
        log.debug("Changing password for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found with ID: " + userId, HttpStatus.NOT_FOUND));
        
        // Encode the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        // Clear the password change requirement after successful change
        user.setRequirePasswordChange(false);
        
        User updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", userId);
        return updatedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPasswordChangeRequired(Long userId) {
        log.debug("Checking password change requirement for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found with ID: " + userId, HttpStatus.NOT_FOUND));
        return user.getRequirePasswordChange();
    }
}
