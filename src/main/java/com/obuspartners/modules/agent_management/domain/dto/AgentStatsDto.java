package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for agent statistics
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentStatsDto {

    private Long totalAgents;
    private Long activeAgents;
    private Long inactiveAgents;
    private Long pendingAgents;
    private Long suspendedAgents;
    private Long deletedAgents;
    
    private Map<AgentStatus, Long> statusCounts;
    
    private Long individualAgents;
    private Long corporateAgents;
    private Long totalPartners;
    
    private Long agentsCreatedThisMonth;
    private Long agentsCreatedThisWeek;
    private Long agentsCreatedToday;
    
    private Double averageAgentsPerPartner;
    private Long partnerWithMostAgents;
    private String partnerWithMostAgentsName;
    
    private Long totalSuperAgents;
    private Long totalSubAgents;
}
