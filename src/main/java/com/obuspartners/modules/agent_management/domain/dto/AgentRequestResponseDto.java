package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.AgentRequestStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent Request Response DTO
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequestResponseDto {
    
    private Long id;
    private String uid;
    private Long partnerId;
    private String partnerCode;
    private String partnerBusinessName;
    private String partnerAgentNumber;
    private String businessName;
    private String contactPerson;
    private String phoneNumber;
    private String msisdn;
    private String businessEmail;
    private String businessAddress;
    private String taxId;
    private String licenseNumber;
    private AgentType agentType;
    private Long superAgentId;
    private String superAgentCode;
    private String superAgentBusinessName;
    private String notes;
    private AgentRequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String processedBy;
    private String rejectionReason;
    private String verificationReferenceNumber;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
