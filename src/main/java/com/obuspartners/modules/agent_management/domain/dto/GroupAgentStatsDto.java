package com.obuspartners.modules.agent_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for GroupAgent statistics
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupAgentStatsDto {

    // Total counts
    private long totalGroupAgents;
    private long activeGroupAgents;
    private long suspendedGroupAgents;
    private long inactiveGroupAgents;
    
    // Type counts
    private long standardGroupAgents;
    private long premiumGroupAgents;
    private long enterpriseGroupAgents;
    private long franchiseGroupAgents;
    private long corporateGroupAgents;
    
    // Bus core system counts
    private long totalBusCoreSystemAssignments;
    private long activeBusCoreSystemAssignments;
    
    // Agent counts
    private long totalAssignedAgents;
    private long activeAssignedAgents;
    
    // Recent activity
    private long groupAgentsWithRecentActivity;
    private long recentBusCoreSystemAuthentications;
    private long recentBusCoreSystemSyncs;
}
