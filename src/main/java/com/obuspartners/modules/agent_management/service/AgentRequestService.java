package com.obuspartners.modules.agent_management.service;

import com.obuspartners.modules.agent_management.domain.dto.AgentRequestResponseDto;
import com.obuspartners.modules.agent_management.domain.dto.AgentRequestSearchRequestDto;
import com.obuspartners.modules.agent_management.domain.dto.AgentRequestStatsDto;
import com.obuspartners.modules.agent_management.domain.dto.CreateAgentRequestDto;
import com.obuspartners.modules.agent_management.domain.enums.AgentRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for AgentRequest operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface AgentRequestService {
    
    /**
     * Create a new agent request
     */
    AgentRequestResponseDto createAgentRequest(CreateAgentRequestDto createRequest);
    
    /**
     * Get agent request by ID
     */
    Optional<AgentRequestResponseDto> getAgentRequestById(Long id);
    
    /**
     * Get agent request by UID
     */
    Optional<AgentRequestResponseDto> getAgentRequestByUid(String uid);
    
    /**
     * Get all agent requests with pagination
     */
    Page<AgentRequestResponseDto> getAllAgentRequests(Pageable pageable);
    
    /**
     * Get agent requests by partner with pagination
     */
    Page<AgentRequestResponseDto> getAgentRequestsByPartner(Long partnerId, Pageable pageable);
    
    /**
     * Get agent requests by status with pagination
     */
    Page<AgentRequestResponseDto> getAgentRequestsByStatus(AgentRequestStatus status, Pageable pageable);
    
    /**
     * Search agent requests
     */
    Page<AgentRequestResponseDto> searchAgentRequests(AgentRequestSearchRequestDto searchRequest, Pageable pageable);
    
    /**
     * Approve agent request (creates actual Agent entity)
     */
    AgentRequestResponseDto approveAgentRequest(String uid, String processedBy);
    
    /**
     * Reject agent request
     */
    AgentRequestResponseDto rejectAgentRequest(String uid, String processedBy, String rejectionReason);
    
    /**
     * Cancel agent request
     */
    AgentRequestResponseDto cancelAgentRequest(String uid, String processedBy);
    
    /**
     * Get expired agent requests
     */
    List<AgentRequestResponseDto> getExpiredAgentRequests();
    
    /**
     * Process expired agent requests (mark as expired)
     */
    void processExpiredAgentRequests();
    
    /**
     * Get agent request statistics
     */
    AgentRequestStatsDto getAgentRequestStats();
    
    /**
     * Get agent request statistics by partner
     */
    AgentRequestStatsDto getAgentRequestStatsByPartner(Long partnerId);
}
