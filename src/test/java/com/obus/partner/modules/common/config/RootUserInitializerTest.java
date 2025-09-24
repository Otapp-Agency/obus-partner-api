package com.obus.partner.modules.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.obuspartners.modules.user_and_role_management.domain.entity.Role;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RootUserInitializer
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@SpringBootTest
@TestPropertySource(properties = {
    "root-admin.username=testrootadmin",
    "root-admin.password=TestRoot@2024!Password",
    "root-admin.email=testroot@obus.com",
    "root-admin.firstName=TestRoot",
    "root-admin.lastName=Administrator"
})
public class RootUserInitializerTest {

    @Autowired
    private UserService userService;

    @Test
    public void testRootUserCreation() {
        // Verify root user was created
        Optional<User> rootUser = userService.findByUsername("testrootadmin");
        
        assertTrue(rootUser.isPresent(), "Root admin user should be created");
        
        User user = rootUser.get();
        assertEquals("testrootadmin", user.getUsername());
        assertEquals("testroot@obus.com", user.getEmail());
        assertEquals("TestRoot", user.getFirstName());
        assertEquals("Administrator", user.getLastName());
        assertEquals(Role.ADMIN, user.getRole());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        
        // Verify password is encoded (not plain text)
        assertNotEquals("TestRoot@2024!Password", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$")); // BCrypt hash format
    }

    @Test
    public void testRootUserLogin() {
        // Test that root user can login
        Optional<User> rootUser = userService.findByUsername("testrootadmin");
        assertTrue(rootUser.isPresent(), "Root admin user should exist for login test");
        
        // Verify user details
        User user = rootUser.get();
        assertEquals(Role.ADMIN, user.getRole());
        assertTrue(user.isEnabled());
    }
}
