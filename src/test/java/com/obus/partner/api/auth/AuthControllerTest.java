package com.obus.partner.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obuspartners.modules.auth_management.domain.dto.LoginRequest;
import com.obuspartners.modules.auth_management.domain.dto.RefreshTokenRequest;
import com.obuspartners.modules.auth_management.domain.dto.RegisterRequestDto;
import com.obuspartners.modules.user_and_role_management.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for AuthController
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureWebMvc
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testRegisterUser() throws Exception {
        setup();
        
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User"
        );

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    public void testLoginUser() throws Exception {
        setup();
        
        // First register a user
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "logintest",
            "logintest@example.com",
            "password123",
            "Login",
            "Test"
        );

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Then login
        LoginRequest loginRequest = new LoginRequest("logintest", "password123");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.username").value("logintest"))
                .andExpect(jsonPath("$.email").value("logintest@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        setup();
        
        LoginRequest loginRequest = new LoginRequest("nonexistent", "wrongpassword");

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid username or password"));
    }

    @Test
    public void testRegisterWithExistingUsername() throws Exception {
        setup();
        
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "existinguser",
            "existing@example.com",
            "password123",
            "Existing",
            "User"
        );

        // First registration should succeed
        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Second registration with same username should fail
        RegisterRequestDto duplicateRequest = new RegisterRequestDto(
            "existinguser",
            "different@example.com",
            "password123",
            "Different",
            "User"
        );

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken!"));
    }

    @Test
    public void testRefreshToken() throws Exception {
        setup();
        
        // First register and login to get tokens
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "refreshtest",
            "refreshtest@example.com",
            "password123",
            "Refresh",
            "Test"
        );

        mockMvc.perform(post("/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest("refreshtest", "password123");
        
        String loginResponse = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn().getResponse().getContentAsString();

        // Extract refresh token from response (simplified - in real test you'd parse JSON)
        // For now, we'll just test that the endpoint exists
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("dummy_refresh_token");
        
        mockMvc.perform(post("/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized()); // Should fail with invalid token
    }

    @Test
    public void testLogout() throws Exception {
        setup();
        
        RefreshTokenRequest logoutRequest = new RefreshTokenRequest("dummy_refresh_token");
        
        mockMvc.perform(post("/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));
    }
}