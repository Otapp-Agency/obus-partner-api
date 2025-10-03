package com.obuspartners.modules.agent_management.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating GroupAgentCoreBusSystem credentials
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupAgentCoreBusSystemRequest {

    @Size(max = 100, message = "External agent identifier must not exceed 100 characters")
    private String externalAgentIdentifier;

    @Size(max = 100, message = "Username must not exceed 100 characters")
    private String username;

    @Size(max = 255, message = "Password must not exceed 255 characters")
    private String password;

    @Size(max = 500, message = "API key must not exceed 500 characters")
    private String apiKey;

    @Size(max = 500, message = "API secret must not exceed 500 characters")
    private String apiSecret;

    @Size(max = 1000, message = "Access token must not exceed 1000 characters")
    private String accessToken;

    @Size(max = 1000, message = "Refresh token must not exceed 1000 characters")
    private String refreshToken;

    private Boolean isActive;
    private Boolean isPrimary;

    @Size(max = 50, message = "External system status must not exceed 50 characters")
    private String externalSystemStatus;

    @Size(max = 100, message = "External agent ID must not exceed 100 characters")
    private String externalAgentId;

    // Additional configuration
    private String configuration;
    private String endpointUrl;
    private Integer timeoutSeconds;
    private Integer retryAttempts;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
