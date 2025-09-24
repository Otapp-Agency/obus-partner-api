package com.obuspartners.modules.auth_management.domain.dto;

/**
 * Authentication response DTO
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String email;
    private String role;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String accessToken, String username, String email, String role) {
        this.accessToken = accessToken;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public AuthResponse(String accessToken, String refreshToken, String username, String email, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
