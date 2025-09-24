package com.obuspartners.modules.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.obuspartners.modules.user_and_role_management.domain.entity.Role;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.service.UserService;

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

    @Value("${root-admin.username}")
    private String rootUsername;

    @Value("${root-admin.password}")
    private String rootPassword;

    @Value("${root-admin.email}")
    private String rootEmail;

    @Value("${root-admin.firstName}")
    private String rootFirstName;

    @Value("${root-admin.lastName}")
    private String rootLastName;

    @Override
    public void run(String... args) throws Exception {
        initializeRootUser();
    }

    private void initializeRootUser() {
        try {
            log.info("Checking for root admin user...");
            
            // Check if root admin user already exists
            if (userService.existsByUsername(rootUsername)) {
                log.info("Root admin user '{}' already exists. Skipping initialization.", rootUsername);
                return;
            }

            // Check if any admin user exists
            if (userService.existsByEmail(rootEmail)) {
                log.info("Root admin email '{}' already exists. Skipping initialization.", rootEmail);
                return;
            }

            // Create root admin user
            User rootUser = new User(
                rootUsername,
                rootEmail,
                rootPassword,
                rootFirstName,
                rootLastName
            );
            
            rootUser.setRole(Role.ADMIN);
            rootUser.setEnabled(true);
            rootUser.setAccountNonExpired(true);
            rootUser.setAccountNonLocked(true);
            rootUser.setCredentialsNonExpired(true);

            // Save the root user (password will be encoded in UserService)
            User savedUser = userService.save(rootUser);
            
            log.info("✅ Root admin user created successfully!");
            log.info("   Username: {}", savedUser.getUsername());
            log.info("   Email: {}", savedUser.getEmail());
            log.info("   Role: {}", savedUser.getRole());
            log.info("   ID: {}", savedUser.getId());
            
        } catch (Exception e) {
            log.error("❌ Failed to create root admin user: {}", e.getMessage(), e);
        }
    }
}
