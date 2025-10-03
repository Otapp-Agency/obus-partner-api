package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.GroupAgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.GroupAgentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for GroupAgent search request
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupAgentSearchRequestDto {

    private Long partnerId;
    private String partnerUid;
    
    // Search filters
    private String searchTerm;
    private GroupAgentStatus status;
    private GroupAgentType type;
    
    // Date filters
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime lastActivityFrom;
    private LocalDateTime lastActivityTo;
    
    // Pagination
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 20;
    @Builder.Default
    private String sortBy = "createdAt";
    @Builder.Default
    private String sortDirection = "DESC";
    
    // Include related data
    @Builder.Default
    private Boolean includeBusCoreSystems = false;
    @Builder.Default
    private Boolean includeAgentCount = true;
}
