package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for agent response data
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponseDto {

    private Long id;
    private String uid;
    private String code;
    private String partnerAgentNumber;
    private String loginUsername;
    private String loginPassword;
    private String businessName;
    private String contactPerson;
    private String phoneNumber;
    private String businessEmail;
    private String businessAddress;
    private String taxId;
    private String licenseNumber;
    private AgentType agentType;
    private AgentStatus status;
    private LocalDateTime registrationDate;
    private LocalDateTime approvalDate;
    private LocalDateTime lastActivityDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Partner information
    private Long partnerId;
    private String partnerUid;
    private String partnerCode;
    private String partnerBusinessName;
    
    // Super agent information (for sub-agents)
    private Long superAgentId;
    private String superAgentUid;
    private String superAgentCode;
    private String superAgentBusinessName;
    
    // User information (if linked)
    private Long userId;
    private String userUsername;
    private String userEmail;
    
    // Manual getter and setter for loginPassword to ensure it works
    public String getLoginPassword() {
        return loginPassword;
    }
    
    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }
}
