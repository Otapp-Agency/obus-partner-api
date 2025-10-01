package com.obuspartners.modules.agent_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for agent login response
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentLoginResponseDto {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private String passName;
    private String partnerAgentNumber;
    private String partnerCode;
    private String email;
    private String userType;
    private boolean requireResetPassword;
    private Long partnerId;
    private String partnerUid;
    private String partnerBusinessName;
}
