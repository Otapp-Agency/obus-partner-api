package com.obuspartners.api.admin;

import com.obuspartners.modules.agent_management.domain.dto.*;
import com.obuspartners.modules.agent_management.domain.entity.AgentBusCoreSystem;
import com.obuspartners.modules.agent_management.service.AgentBusCoreSystemService;
import com.obuspartners.modules.agent_management.service.AgentService;
import com.obuspartners.modules.bus_core_system.domain.dto.BusCoreSystemResponseDto;
import com.obuspartners.modules.bus_core_system.service.BusCoreSystemService;
import com.obuspartners.modules.common.exception.ResourceNotFoundException;
import com.obuspartners.modules.common.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing Agent-BusCoreSystem relationships
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/v1/agent-bus-core-systems")
@RequiredArgsConstructor
@Tag(name = "Admin Agent Bus Core System Management", description = "Administrative endpoints for managing agent-bus core system relationships")
public class AdminAgentBusCoreSystemController {

    private final AgentBusCoreSystemService agentBusCoreSystemService;
    private final AgentService agentService;
    private final BusCoreSystemService busCoreSystemService;

    @PostMapping("/assign")
    @Operation(summary = "Assign agent to bus core system", description = "Assigns an agent to a bus core system with specific credentials and permissions")
    public ResponseEntity<ResponseWrapper<AgentBusCoreSystemResponseDto>> assignAgentToBusCoreSystem(
            @Valid @RequestBody AssignAgentToBusCoreSystemRequest request) {
        
        log.info("Assigning agent {} to bus core system {}", request.getAgentId(), request.getBusCoreSystemId());
        
        AgentBusCoreSystemResponseDto response = agentBusCoreSystemService.assignAgentToBusCoreSystem(request);
        
        log.info("Agent assigned successfully to bus core system");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseWrapper<>(true, 201, "Agent assigned to bus core system successfully", response));
    }

    @GetMapping("/agent/{agentId}")
    @Operation(summary = "Get bus core systems for agent", description = "Retrieves all bus core systems assigned to a specific agent")
    public ResponseEntity<ResponseWrapper<List<AgentBusCoreSystemResponseDto>>> getBusCoreSystemsByAgent(
            @Parameter(description = "Agent ID") @PathVariable Long agentId) {
        
        log.info("Retrieving bus core systems for agent {}", agentId);
        
        // Verify agent exists
        agentService.getAgentById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with ID: " + agentId));
        
        List<AgentBusCoreSystemResponseDto> response = agentBusCoreSystemService.getBusCoreSystemsByAgentId(agentId);
        
        log.info("Retrieved {} bus core systems for agent {}", response.size(), agentId);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Bus core systems retrieved successfully", response));
    }

    @GetMapping("/agent/uid/{agentUid}")
    @Operation(summary = "Get bus core systems for agent by UID", description = "Retrieves all bus core systems assigned to a specific agent using agent UID")
    public ResponseEntity<ResponseWrapper<List<AgentBusCoreSystemResponseDto>>> getBusCoreSystemsByAgentUid(
            @Parameter(description = "Agent UID") @PathVariable String agentUid) {
        
        log.info("Retrieving bus core systems for agent UID {}", agentUid);
        
        // Verify agent exists by UID
        agentService.getAgent(agentUid)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with UID: " + agentUid));
        
        List<AgentBusCoreSystemResponseDto> response = agentBusCoreSystemService.getBusCoreSystemsByAgentUid(agentUid);
        
        log.info("Retrieved {} bus core systems for agent UID {}", response.size(), agentUid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Bus core systems retrieved successfully", response));
    }

    @GetMapping("/bus-core-system/{busCoreSystemId}")
    @Operation(summary = "Get agents for bus core system", description = "Retrieves all agents assigned to a specific bus core system")
    public ResponseEntity<ResponseWrapper<List<AgentBusCoreSystemResponseDto>>> getAgentsByBusCoreSystem(
            @Parameter(description = "Bus Core System ID") @PathVariable Long busCoreSystemId) {
        
        log.info("Retrieving agents for bus core system {}", busCoreSystemId);
        
        BusCoreSystemResponseDto busCoreSystemDto = busCoreSystemService.getById(busCoreSystemId);
        if (busCoreSystemDto == null) {
            throw new ResourceNotFoundException("Bus Core System not found with ID: " + busCoreSystemId);
        }
        
        List<AgentBusCoreSystemResponseDto> response = agentBusCoreSystemService.getAgentsByBusCoreSystemId(busCoreSystemId);
        
        log.info("Retrieved {} agents for bus core system {}", response.size(), busCoreSystemId);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Agents retrieved successfully", response));
    }

    @PutMapping("/uid/{uid}")
    @Operation(summary = "Update agent bus core system configuration", description = "Updates the configuration and permissions for an agent-bus core system relationship")
    public ResponseEntity<ResponseWrapper<AgentBusCoreSystemResponseDto>> updateAgentBusCoreSystem(
            @Parameter(description = "Agent-Bus Core System relationship UID") @PathVariable String uid,
            @Valid @RequestBody UpdateAgentBusCoreSystemRequest request) {
        
        log.info("Updating agent bus core system configuration {}", uid);
        
        AgentBusCoreSystemResponseDto response = agentBusCoreSystemService.updateAgentBusCoreSystemByUid(uid, request);
        
        log.info("Agent bus core system configuration updated successfully");
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Configuration updated successfully", response));
    }

    @PutMapping("/uid/{uid}/activate")
    @Operation(summary = "Activate/deactivate agent for bus core system", description = "Activates or deactivates an agent for a specific bus core system")
    public ResponseEntity<ResponseWrapper<AgentBusCoreSystemResponseDto>> setAgentActiveStatus(
            @Parameter(description = "Agent-Bus Core System relationship UID") @PathVariable String uid,
            @Parameter(description = "Active status") @RequestParam Boolean isActive) {
        
        log.info("Setting agent bus core system {} active status to {}", uid, isActive);
        
        AgentBusCoreSystemResponseDto response = agentBusCoreSystemService.setAgentActiveStatusByUid(uid, isActive);
        
        log.info("Agent active status updated successfully");
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Status updated successfully", response));
    }

    @PutMapping("/uid/{uid}/primary")
    @Operation(summary = "Set primary bus core system for agent", description = "Sets a bus core system as the primary one for an agent")
    public ResponseEntity<ResponseWrapper<AgentBusCoreSystemResponseDto>> setPrimaryBusCoreSystem(
            @Parameter(description = "Agent-Bus Core System relationship UID") @PathVariable String uid) {
        
        log.info("Setting agent bus core system {} as primary", uid);
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemService.findEntityByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Agent-Bus Core System relationship not found"));
        
        AgentBusCoreSystemResponseDto response = agentBusCoreSystemService.setPrimaryBusCoreSystem(
                agentBusCoreSystem.getAgent(), agentBusCoreSystem.getBusCoreSystem());
        
        log.info("Primary bus core system updated successfully");
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Primary status updated successfully", response));
    }

    @DeleteMapping("/uid/{uid}")
    @Operation(summary = "Remove agent from bus core system", description = "Removes an agent from a bus core system")
    public ResponseEntity<Void> removeAgentFromBusCoreSystem(
            @Parameter(description = "Agent-Bus Core System relationship UID") @PathVariable String uid) {
        
        log.info("Removing agent bus core system relationship {}", uid);
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemService.findEntityByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Agent-Bus Core System relationship not found"));
        
        agentBusCoreSystemService.removeAgentFromBusCoreSystem(
                agentBusCoreSystem.getAgent(), agentBusCoreSystem.getBusCoreSystem());
        
        log.info("Agent removed from bus core system successfully");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/uid/{uid}/permissions")
    @Operation(summary = "Get agent permissions for bus core system", description = "Retrieves the permissions and configuration for an agent on a specific bus core system")
    public ResponseEntity<ResponseWrapper<AgentBusCoreSystemResponseDto>> getAgentPermissions(
            @Parameter(description = "Agent-Bus Core System relationship UID") @PathVariable String uid) {
        
        log.info("Retrieving permissions for agent bus core system {}", uid);
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemService.findEntityByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Agent-Bus Core System relationship not found"));
        
        AgentBusCoreSystemResponseDto response = agentBusCoreSystemService.getAgentPermissions(
                agentBusCoreSystem.getAgent(), agentBusCoreSystem.getBusCoreSystem());
        
        log.info("Agent permissions retrieved successfully");
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Permissions retrieved successfully", response));
    }

}
