package com.obuspartners.modules.agent_management.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for assigning GroupAgent to BusCoreSystem
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignGroupAgentToBusCoreSystemRequest {

    @NotNull(message = "Group agent ID is required")
    private Long groupAgentId;

    @NotNull(message = "Bus core system ID is required")
    private Long busCoreSystemId;

    @NotBlank(message = "External agent identifier is required")
    @Size(max = 100, message = "External agent identifier must not exceed 100 characters")
    private String externalAgentIdentifier;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password must not exceed 255 characters")
    private String password;

    @Size(max = 100, message = "Transaction username must not exceed 100 characters")
    private String txnUserName;

    @Size(max = 255, message = "Transaction password must not exceed 255 characters")
    private String txnPassword;

    @Size(max = 500, message = "API key must not exceed 500 characters")
    private String apiKey;

    @Size(max = 500, message = "API secret must not exceed 500 characters")
    private String apiSecret;

    @Builder.Default
    private Boolean isPrimary = false;

    // Additional configuration
    private String configuration;
    private String endpointUrl;
    @Builder.Default
    private Integer timeoutSeconds = 30;
    @Builder.Default
    private Integer retryAttempts = 3;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
