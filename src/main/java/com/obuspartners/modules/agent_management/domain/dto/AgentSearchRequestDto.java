package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * DTO for agent search requests with multiple criteria
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSearchRequestDto {

    private String businessName;
    private String contactPerson;
    private String code;
    private String partnerAgentNumber;
    private AgentStatus status;
    private AgentType agentType;
    private Long partnerId;
    private Long superAgentId;
    private LocalDateTime registrationDateFrom;
    private LocalDateTime registrationDateTo;
    private LocalDateTime lastActivityDateFrom;
    private LocalDateTime lastActivityDateTo;
    private Pageable pageable;
}
