package com.obuspartners.api.admin;

import com.obuspartners.modules.agent_management.domain.dto.*;
import com.obuspartners.modules.agent_management.service.AgentService;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.common.util.PageResponseWrapper;
import com.obuspartners.modules.common.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Admin Agent Management API endpoints
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/v1/agents")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Admin Agent Management", description = "Agent management operations for administrators")
public class AdminAgentController {

    private final AgentService agentService;

    /**
     * Get all agents with pagination
     */
    @GetMapping
    @Operation(summary = "Get all agents with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Agents retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    })
    public ResponseEntity<PageResponseWrapper<AgentSummaryDto>> getAllAgents(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AgentSummaryDto> agents = agentService.getAllAgentSummaries(pageable);
        return ResponseEntity.ok(PageResponseWrapper.fromPage(agents, "Agents retrieved successfully"));
    }

    /**
     * Get agents by partner UID
     */
    @GetMapping("/partner/uid/{partnerUid}")
    @Operation(summary = "Get agents by partner UID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Agents retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Partner not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    })
    public ResponseEntity<PageResponseWrapper<AgentSummaryDto>> getAgentsByPartner(
            @Parameter(description = "Partner UID") @PathVariable String partnerUid,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AgentSummaryDto> agents = agentService.getAgentsByPartner(partnerUid, pageable);
        return ResponseEntity.ok(PageResponseWrapper.fromPage(agents, "Agents retrieved successfully for partner"));
    }

    /**
     * Update agent status (Admin only)
     */
    @PutMapping("/uid/{uid}/status")
    @Operation(summary = "Update agent status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Agent status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Agent not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status or request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    })
    public ResponseEntity<ResponseWrapper<AgentResponseDto>> updateAgentStatus(
            @Parameter(description = "Agent UID") @PathVariable String uid,
            @Valid @RequestBody UpdateAgentStatusRequestDto updateRequest) {

        AgentResponseDto updatedAgent = agentService.updateAgentStatus(uid, updateRequest.getStatus());
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Agent status updated successfully", updatedAgent));
    }

    /**
     * Create a new super agent (Admin only)
     */
    @PostMapping("/super-agent")
    @Operation(summary = "Create a new super agent")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "201", description = "Super agent created successfully"),
    //     @ApiResponse(responseCode = "400", description = "Invalid request data"),
    //     @ApiResponse(responseCode = "409", description = "Username, email, or agent number already exists"),
    //     @ApiResponse(responseCode = "404", description = "Partner not found"),
    //     @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    // })
    public ResponseEntity<ResponseWrapper<AgentResponseDto>> createSuperAgent(
            @Valid @RequestBody CreateSuperAgentRequestDto createRequest) {

        AgentResponseDto createdSuperAgent = agentService.createSuperAgent(createRequest);
        return ResponseEntity.status(201)
                .body(new ResponseWrapper<>(true, 201, "Super agent created successfully", createdSuperAgent));
    }
}