package com.obuspartners.modules.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.obuspartners.modules.user_and_role_management.domain.enums.UserType;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserStatus;
import com.obuspartners.modules.user_and_role_management.domain.entity.SystemUser;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.service.UserService;
import com.obuspartners.modules.user_and_role_management.repository.SystemUserRepository;
import com.obuspartners.modules.user_and_role_management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Root user initializer - creates the root admin user on application startup
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RootUserInitializer implements CommandLineRunner {

    private final UserService userService;
    private final SystemUserRepository systemUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${root-admin.username}")
    private String rootUsername;

    @Value("${root-admin.password}")
    private String rootPassword;

    @Value("${root-admin.email}")
    private String rootEmail;

    @Value("${root-admin.displayName}")
    private String rootDisplayName;

    @Value("${root-admin.firstName:Root}")
    private String rootFirstName;

    @Value("${root-admin.lastName:Administrator}")
    private String rootLastName;


    @Override
    public void run(String... args) throws Exception {
        initializeRootUser();
        fixRootUserRelationship();
    }

    private void initializeRootUser() {
        try {
            log.info("Checking for root admin user...");
            log.info("Root username: {}", rootUsername);
            log.info("Root email: {}", rootEmail);
            
            // Check if root admin user already exists
            boolean usernameExists = userService.existsByUsername(rootUsername);
            log.info("Username '{}' exists: {}", rootUsername, usernameExists);
            
            if (usernameExists) {
                log.info("Root admin user '{}' already exists. Skipping initialization.", rootUsername);
                return;
            }

            // Check if any admin user exists
            boolean emailExists = userService.existsByEmail(rootEmail);
            log.info("Email '{}' exists: {}", rootEmail, emailExists);
            
            if (emailExists) {
                log.info("Root admin email '{}' already exists. Skipping initialization.", rootEmail);
                return;
            }

            log.info("Creating root admin user...");
            
            // Create User entity with configured password
            User user = new User();
            user.setUsername(rootUsername);
            user.setEmail(rootEmail);
            user.setPassword(passwordEncoder.encode(rootPassword)); // Use configured password
            user.setDisplayName(rootDisplayName);
            user.setUserType(UserType.ADMIN_USER);
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setRequirePasswordChange(false);
            user.setCreatedAt(LocalDateTime.now());
            user = userRepository.save(user);

            log.info("Creating SystemUser entity...");
            
            // Create SystemUser entity (UID will be generated automatically in @PrePersist)
            SystemUser systemUser = new SystemUser();
            systemUser.setUser(user);
            systemUser.setFirstName(rootFirstName);
            systemUser.setLastName(rootLastName);
            systemUser.setDisplayName(rootDisplayName);
            systemUser.setPhoneNumber("+1234567890");
            systemUser.setPersonalEmail(rootEmail);
            systemUser.setEmployeeId("ROOT001");
            systemUser.setDepartment("System Administration");
            systemUser.setPosition("Root Administrator");
            systemUser.setOfficeLocation("Head Office");
            systemUser.setWorkPhone("+1234567890");
            systemUser.setWorkEmail(rootEmail);
            systemUser.setAddress("System Address");
            systemUser.setCity("System City");
            systemUser.setState("System State");
            systemUser.setCountry("System Country");
            systemUser.setPostalCode("00000");
            systemUser.setNationalId("ROOT-ADMIN-ID");
            systemUser.setPassportNumber("ROOT-PASS");
            systemUser.setGender("Not Specified");
            systemUser.setPreferredLanguage("en");
            systemUser.setTimezone("UTC");
            systemUser.setEmergencyContactName("System Administrator");
            systemUser.setEmergencyContactPhone("+1234567890");
            systemUser.setStatus(UserStatus.ACTIVE);
            systemUser.setEmailVerified(true);
            systemUser.setPhoneVerified(false);
            systemUser.setRegistrationDate(LocalDateTime.now());
            systemUser.setCreatedAt(LocalDateTime.now());

            log.info("Saving SystemUser entity first...");
            SystemUser rootSystemUser;
            try {
                rootSystemUser = systemUserRepository.save(systemUser);
                log.info("SystemUser entity saved with ID: {}", rootSystemUser.getId());
            } catch (Exception e) {
                log.error("❌ Failed to save SystemUser entity: {}", e.getMessage(), e);
                throw e; // Re-throw to be caught by outer catch block
            }

            log.info("Linking User and SystemUser entities...");
            
            // Link User and SystemUser (after SystemUser is saved)
            user.setSystemUser(rootSystemUser);
            rootSystemUser.setUser(user);

            log.info("Saving User entity...");
            // Save User entity to persist the relationship
            user = userRepository.save(user);
            log.info("User entity saved with ID: {}", user.getId());
            
            log.info("✅ Root admin user created successfully!");
            log.info("   Generated UID: {}", rootSystemUser.getUid());
            log.info("   Username: {}", rootSystemUser.getUser().getUsername());
            log.info("   Email: {}", rootSystemUser.getUser().getEmail());
            log.info("   Password: {} (from configuration)", rootPassword);
            log.info("   User Type: {}", rootSystemUser.getUser().getUserType());
            log.info("   Name: {} {}", rootSystemUser.getFirstName(), rootSystemUser.getLastName());
            log.info("   Department: {}", rootSystemUser.getDepartment());
            log.info("   Position: {}", rootSystemUser.getPosition());
            log.info("   User ID: {}", rootSystemUser.getUser().getId());
            log.info("   SystemUser ID: {}", rootSystemUser.getId());
            
        } catch (Exception e) {
            log.error("❌ Failed to create root admin user: {}", e.getMessage(), e);
        }
    }

    private void fixRootUserRelationship() {
        try {
            log.info("Checking root user relationship...");
            
            // Find the root user by username
            Optional<User> rootUserOpt = userRepository.findByUsername(rootUsername);
            if (rootUserOpt.isPresent()) {
                User rootUser = rootUserOpt.get();
                
                // Check if the SystemUser relationship is properly loaded
                if (rootUser.getSystemUser() == null) {
                    log.warn("Root user SystemUser relationship is null, attempting to fix...");
                    
                    // Try to find the SystemUser by employee ID
                    Optional<SystemUser> systemUserOpt = systemUserRepository.findByEmployeeId("ROOT001");
                    if (systemUserOpt.isPresent()) {
                        SystemUser systemUser = systemUserOpt.get();
                        
                        // Link the entities
                        rootUser.setSystemUser(systemUser);
                        systemUser.setUser(rootUser);
                        
                        // Save both entities
                        userRepository.save(rootUser);
                        systemUserRepository.save(systemUser);
                        
                        log.info("✅ Root user relationship fixed successfully!");
                    } else {
                        log.error("❌ Could not find SystemUser with employee ID ROOT001");
                    }
                } else {
                    log.info("✅ Root user relationship is already properly established");
                }
            } else {
                log.info("Root user not found, skipping relationship fix");
            }
        } catch (Exception e) {
            log.error("❌ Failed to fix root user relationship: {}", e.getMessage(), e);
        }
    }
}
