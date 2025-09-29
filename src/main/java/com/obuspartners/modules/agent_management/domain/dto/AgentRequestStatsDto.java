package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.AgentRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Agent Request Statistics DTO
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequestStatsDto {
    
    private long totalRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;
    private long cancelledRequests;
    private long expiredRequests;
    
    private Map<AgentRequestStatus, Long> statusCounts;
    
    private double approvalRate;
    private double rejectionRate;
    
    private long requestsLast24Hours;
    private long requestsLast7Days;
    private long requestsLast30Days;
}
