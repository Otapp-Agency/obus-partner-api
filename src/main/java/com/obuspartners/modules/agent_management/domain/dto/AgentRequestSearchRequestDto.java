package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.AgentRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent Request Search Request DTO
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequestSearchRequestDto {
    
    private Long partnerId;
    private String partnerCode;
    private String partnerAgentNumber;
    private String businessName;
    private String contactPerson;
    private String phoneNumber;
    private String msisdn;
    private String businessEmail;
    private AgentRequestStatus status;
    private String processedBy;
    private String rejectionReason;
    private String verificationReferenceNumber;
}
