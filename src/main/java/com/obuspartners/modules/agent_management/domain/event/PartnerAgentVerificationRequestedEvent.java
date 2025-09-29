package com.obuspartners.modules.agent_management.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Partner Agent Verification Requested Event
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerAgentVerificationRequestedEvent {
    
    private String eventId;
    private String eventType;
    
    // Agent information
    private String agentUid;
    private String agentCode;
    private String agentBusinessName;
    private String agentContactPerson;
    private String agentMsisdn;
    private String agentBusinessEmail;
    
    // Partner information
    private String partnerUid;
    private String partnerCode;
    private String partnerBusinessName;
    
    // Verification information
    private String verificationUid;
    private String requestReferenceNumber;
    private String verificationType;
    private String requestedBy;
    private String priority;
    private String externalReference;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Map<String, Object> metadata;
    
    public static PartnerAgentVerificationRequestedEvent create(
            String agentUid, String agentCode, String agentBusinessName,
            String partnerUid, String partnerCode, String partnerBusinessName,
            String verificationUid, String requestReferenceNumber, String requestedBy) {
        
        return PartnerAgentVerificationRequestedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("PARTNER_AGENT_VERIFICATION_REQUESTED")
                .agentUid(agentUid)
                .agentCode(agentCode)
                .agentBusinessName(agentBusinessName)
                .partnerUid(partnerUid)
                .partnerCode(partnerCode)
                .partnerBusinessName(partnerBusinessName)
                .verificationUid(verificationUid)
                .requestReferenceNumber(requestReferenceNumber)
                .verificationType("DOCUMENT_VERIFICATION")
                .requestedBy(requestedBy)
                .priority("NORMAL")
                .requestedAt(LocalDateTime.now())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
