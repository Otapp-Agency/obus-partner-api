package com.obuspartners.modules.auth_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private String username;
    private String email;
    private String userType;
    private boolean requireResetPassword;
    private Long partnerId;
    private String partnerUid;
    private String partnerCode;
    private String partnerBusinessName;
    
    // Essential frontend fields
    private String displayName;
    private java.util.List<String> roles;
    private String tokenExpiresAt;
    private Long agentId;
    private String agentStatus;
    private String lastLoginAt;
}
