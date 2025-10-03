package com.obuspartners.api.admin;

import com.obuspartners.modules.agent_management.domain.dto.*;
import com.obuspartners.modules.agent_management.domain.entity.GroupAgent;
import com.obuspartners.modules.agent_management.domain.entity.GroupAgentCoreBusSystem;
import com.obuspartners.modules.agent_management.service.GroupAgentCoreBusSystemService;
import com.obuspartners.modules.agent_management.service.GroupAgentService;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import com.obuspartners.modules.bus_core_system.repository.BusCoreSystemRepository;
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
 * REST Controller for Admin GroupAgentCoreBusSystem Management API endpoints
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/v1/group-agents/uid/{groupAgentUid}/bus-core-systems")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Admin GroupAgent Core Bus System Management", description = "GroupAgent BusCoreSystem assignment management for administrators")
public class AdminGroupAgentCoreBusSystemController {

    private final GroupAgentCoreBusSystemService groupAgentCoreBusSystemService;
    private final GroupAgentService groupAgentService;
    private final BusCoreSystemRepository busCoreSystemRepository;

    /**
     * Assign GroupAgent to BusCoreSystem
     */
    @PostMapping
    @Operation(summary = "Assign GroupAgent to BusCoreSystem")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "GroupAgent assigned to BusCoreSystem successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgent or BusCoreSystem not found"),
        @ApiResponse(responseCode = "409", description = "GroupAgent already assigned to this BusCoreSystem")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentCoreBusSystemResponseDto>> assignGroupAgentToBusCoreSystem(
            @Parameter(description = "GroupAgent UID") @PathVariable String groupAgentUid,
            @Valid @RequestBody AssignGroupAgentToBusCoreSystemRequest request) {
        
        log.info("Assigning GroupAgent {} to BusCoreSystem {}", groupAgentUid, request.getBusCoreSystemId());
        
        // Validate GroupAgent exists
        GroupAgent groupAgent = groupAgentService.getGroupAgentByUid(groupAgentUid)
                .orElseThrow(() -> new ApiException("GroupAgent not found with UID: " + groupAgentUid, HttpStatus.NOT_FOUND));
        
        // Validate BusCoreSystem exists
        BusCoreSystem busCoreSystem = busCoreSystemRepository.findById(request.getBusCoreSystemId())
                .orElseThrow(() -> new ApiException("BusCoreSystem not found with ID: " + request.getBusCoreSystemId(), HttpStatus.NOT_FOUND));
        
        // Assign GroupAgent to BusCoreSystem
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemService.assignGroupAgentToBusCoreSystem(
                groupAgent, 
                busCoreSystem,
                request.getExternalAgentIdentifier(),
                request.getUsername(),
                request.getPassword(),
                request.getTxnUserName(),
                request.getTxnPassword(),
                request.getApiKey(),
                request.getApiSecret(),
                request.getIsPrimary()
        );
        
        // Set additional configuration if provided
        if (request.getConfiguration() != null) {
            groupAgentCoreBusSystem.setConfiguration(request.getConfiguration());
        }
        if (request.getEndpointUrl() != null) {
            groupAgentCoreBusSystem.setEndpointUrl(request.getEndpointUrl());
        }
        if (request.getTimeoutSeconds() != null) {
            groupAgentCoreBusSystem.setTimeoutSeconds(request.getTimeoutSeconds());
        }
        if (request.getRetryAttempts() != null) {
            groupAgentCoreBusSystem.setRetryAttempts(request.getRetryAttempts());
        }
        if (request.getNotes() != null) {
            groupAgentCoreBusSystem.setNotes(request.getNotes());
        }
        
        // Save the updated entity
        GroupAgentCoreBusSystem savedGroupAgentCoreBusSystem = groupAgentCoreBusSystemService.updateGroupAgentBusCoreSystemCredentials(groupAgentCoreBusSystem);
        
        GroupAgentCoreBusSystemResponseDto responseDto = mapToResponseDto(savedGroupAgentCoreBusSystem);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseWrapper<>(true, 201, "GroupAgent assigned to BusCoreSystem successfully", responseDto));
    }

    /**
     * Get all BusCoreSystems for a GroupAgent
     */
    @GetMapping
    @Operation(summary = "Get all BusCoreSystems for a GroupAgent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "BusCoreSystems retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgent not found")
    })
    public ResponseEntity<PageResponseWrapper<GroupAgentCoreBusSystemResponseDto>> getBusCoreSystemsByGroupAgent(
            @Parameter(description = "GroupAgent UID") @PathVariable String groupAgentUid,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.debug("Getting BusCoreSystems for GroupAgent UID: {}", groupAgentUid);
        
        GroupAgent groupAgent = groupAgentService.getGroupAgentByUid(groupAgentUid)
                .orElseThrow(() -> new ApiException("GroupAgent not found with UID: " + groupAgentUid, HttpStatus.NOT_FOUND));
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<GroupAgentCoreBusSystem> busCoreSystemsPage = groupAgentCoreBusSystemService.getBusCoreSystemsByGroupAgent(groupAgent, pageable);
        Page<GroupAgentCoreBusSystemResponseDto> responseDtoPage = busCoreSystemsPage.map(this::mapToResponseDto);
        
        return ResponseEntity.ok(PageResponseWrapper.fromPage(responseDtoPage, "BusCoreSystems retrieved successfully"));
    }

    /**
     * Get GroupAgentCoreBusSystem by UID
     */
    @GetMapping("/uid/{uid}")
    @Operation(summary = "Get GroupAgentCoreBusSystem by UID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgentCoreBusSystem retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgentCoreBusSystem not found")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentCoreBusSystemResponseDto>> getGroupAgentCoreBusSystemByUid(
            @Parameter(description = "GroupAgent UID") @PathVariable String groupAgentUid,
            @Parameter(description = "GroupAgentCoreBusSystem UID") @PathVariable String uid) {
        
        log.debug("Getting GroupAgentCoreBusSystem by UID: {}", uid);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemService.getGroupAgentCoreBusSystemByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgentCoreBusSystem not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Verify it belongs to the specified GroupAgent
        if (!groupAgentCoreBusSystem.getGroupAgent().getUid().equals(groupAgentUid)) {
            throw new ApiException("GroupAgentCoreBusSystem does not belong to the specified GroupAgent", HttpStatus.BAD_REQUEST);
        }
        
        GroupAgentCoreBusSystemResponseDto responseDto = mapToResponseDto(groupAgentCoreBusSystem);
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgentCoreBusSystem retrieved successfully", responseDto));
    }

    /**
     * Update GroupAgentCoreBusSystem credentials
     */
    @PutMapping("/uid/{uid}")
    @Operation(summary = "Update GroupAgentCoreBusSystem credentials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgentCoreBusSystem updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgentCoreBusSystem not found")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentCoreBusSystemResponseDto>> updateGroupAgentCoreBusSystem(
            @Parameter(description = "GroupAgent UID") @PathVariable String groupAgentUid,
            @Parameter(description = "GroupAgentCoreBusSystem UID") @PathVariable String uid,
            @Valid @RequestBody UpdateGroupAgentCoreBusSystemRequest request) {
        
        log.info("Updating GroupAgentCoreBusSystem with UID: {}", uid);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemService.getGroupAgentCoreBusSystemByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgentCoreBusSystem not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Verify it belongs to the specified GroupAgent
        if (!groupAgentCoreBusSystem.getGroupAgent().getUid().equals(groupAgentUid)) {
            throw new ApiException("GroupAgentCoreBusSystem does not belong to the specified GroupAgent", HttpStatus.BAD_REQUEST);
        }
        
        // Update fields
        if (request.getExternalAgentIdentifier() != null) {
            groupAgentCoreBusSystem.setExternalAgentIdentifier(request.getExternalAgentIdentifier());
        }
        if (request.getUsername() != null) {
            groupAgentCoreBusSystem.setUsername(request.getUsername());
        }
        if (request.getPassword() != null) {
            groupAgentCoreBusSystem.setPassword(request.getPassword());
        }
        if (request.getApiKey() != null) {
            groupAgentCoreBusSystem.setApiKey(request.getApiKey());
        }
        if (request.getApiSecret() != null) {
            groupAgentCoreBusSystem.setApiSecret(request.getApiSecret());
        }
        if (request.getAccessToken() != null) {
            groupAgentCoreBusSystem.setAccessToken(request.getAccessToken());
        }
        if (request.getRefreshToken() != null) {
            groupAgentCoreBusSystem.setRefreshToken(request.getRefreshToken());
        }
        if (request.getIsActive() != null) {
            groupAgentCoreBusSystem.setIsActive(request.getIsActive());
        }
        if (request.getIsPrimary() != null) {
            groupAgentCoreBusSystem.setIsPrimary(request.getIsPrimary());
        }
        if (request.getExternalSystemStatus() != null) {
            groupAgentCoreBusSystem.setExternalSystemStatus(request.getExternalSystemStatus());
        }
        if (request.getExternalAgentId() != null) {
            groupAgentCoreBusSystem.setExternalAgentId(request.getExternalAgentId());
        }
        if (request.getConfiguration() != null) {
            groupAgentCoreBusSystem.setConfiguration(request.getConfiguration());
        }
        if (request.getEndpointUrl() != null) {
            groupAgentCoreBusSystem.setEndpointUrl(request.getEndpointUrl());
        }
        if (request.getTimeoutSeconds() != null) {
            groupAgentCoreBusSystem.setTimeoutSeconds(request.getTimeoutSeconds());
        }
        if (request.getRetryAttempts() != null) {
            groupAgentCoreBusSystem.setRetryAttempts(request.getRetryAttempts());
        }
        if (request.getNotes() != null) {
            groupAgentCoreBusSystem.setNotes(request.getNotes());
        }
        
        GroupAgentCoreBusSystem updatedGroupAgentCoreBusSystem = groupAgentCoreBusSystemService.updateGroupAgentBusCoreSystemCredentials(groupAgentCoreBusSystem);
        GroupAgentCoreBusSystemResponseDto responseDto = mapToResponseDto(updatedGroupAgentCoreBusSystem);
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgentCoreBusSystem updated successfully", responseDto));
    }

    /**
     * Remove GroupAgent from BusCoreSystem
     */
    @DeleteMapping("/uid/{uid}")
    @Operation(summary = "Remove GroupAgent from BusCoreSystem")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgent removed from BusCoreSystem successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgentCoreBusSystem not found")
    })
    public ResponseEntity<ResponseWrapper<String>> removeGroupAgentFromBusCoreSystem(
            @Parameter(description = "GroupAgent UID") @PathVariable String groupAgentUid,
            @Parameter(description = "GroupAgentCoreBusSystem UID") @PathVariable String uid) {
        
        log.info("Removing GroupAgentCoreBusSystem with UID: {}", uid);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemService.getGroupAgentCoreBusSystemByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgentCoreBusSystem not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Verify it belongs to the specified GroupAgent
        if (!groupAgentCoreBusSystem.getGroupAgent().getUid().equals(groupAgentUid)) {
            throw new ApiException("GroupAgentCoreBusSystem does not belong to the specified GroupAgent", HttpStatus.BAD_REQUEST);
        }
        
        groupAgentCoreBusSystemService.deleteGroupAgentCoreBusSystem(groupAgentCoreBusSystem.getId());
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgent removed from BusCoreSystem successfully", "GroupAgent removed from BusCoreSystem successfully"));
    }

    /**
     * Set primary BusCoreSystem for GroupAgent
     */
    @PostMapping("/uid/{uid}/set-primary")
    @Operation(summary = "Set primary BusCoreSystem for GroupAgent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Primary BusCoreSystem set successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgentCoreBusSystem not found")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentCoreBusSystemResponseDto>> setPrimaryBusCoreSystem(
            @Parameter(description = "GroupAgent UID") @PathVariable String groupAgentUid,
            @Parameter(description = "GroupAgentCoreBusSystem UID") @PathVariable String uid) {
        
        log.info("Setting primary BusCoreSystem for GroupAgent {} using assignment UID: {}", groupAgentUid, uid);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemService.getGroupAgentCoreBusSystemByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgentCoreBusSystem not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Verify it belongs to the specified GroupAgent
        if (!groupAgentCoreBusSystem.getGroupAgent().getUid().equals(groupAgentUid)) {
            throw new ApiException("GroupAgentCoreBusSystem does not belong to the specified GroupAgent", HttpStatus.BAD_REQUEST);
        }
        
        GroupAgentCoreBusSystem updatedGroupAgentCoreBusSystem = groupAgentCoreBusSystemService.setPrimaryBusCoreSystem(
                groupAgentCoreBusSystem.getGroupAgent(), 
                groupAgentCoreBusSystem.getBusCoreSystem()
        );
        
        GroupAgentCoreBusSystemResponseDto responseDto = mapToResponseDto(updatedGroupAgentCoreBusSystem);
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Primary BusCoreSystem set successfully", responseDto));
    }

    /**
     * Activate GroupAgent for BusCoreSystem
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate GroupAgent for BusCoreSystem")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgent activated for BusCoreSystem successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgentCoreBusSystem not found")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentCoreBusSystemResponseDto>> activateGroupAgentForBusCoreSystem(
            @Parameter(description = "GroupAgent ID") @PathVariable Long groupAgentId,
            @Parameter(description = "GroupAgentCoreBusSystem ID") @PathVariable Long id) {
        
        log.info("Activating GroupAgent for BusCoreSystem with ID: {}", id);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemService.getGroupAgentCoreBusSystemById(id)
                .orElseThrow(() -> new ApiException("GroupAgentCoreBusSystem not found with ID: " + id, HttpStatus.NOT_FOUND));
        
        // Verify it belongs to the specified GroupAgent
        if (!groupAgentCoreBusSystem.getGroupAgent().getId().equals(groupAgentId)) {
            throw new ApiException("GroupAgentCoreBusSystem does not belong to the specified GroupAgent", HttpStatus.BAD_REQUEST);
        }
        
        GroupAgentCoreBusSystem activatedGroupAgentCoreBusSystem = groupAgentCoreBusSystemService.activateGroupAgentForBusCoreSystem(groupAgentCoreBusSystem);
        GroupAgentCoreBusSystemResponseDto responseDto = mapToResponseDto(activatedGroupAgentCoreBusSystem);
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgent activated for BusCoreSystem successfully", responseDto));
    }

    /**
     * Deactivate GroupAgent for BusCoreSystem
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate GroupAgent for BusCoreSystem")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgent deactivated for BusCoreSystem successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgentCoreBusSystem not found")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentCoreBusSystemResponseDto>> deactivateGroupAgentForBusCoreSystem(
            @Parameter(description = "GroupAgent ID") @PathVariable Long groupAgentId,
            @Parameter(description = "GroupAgentCoreBusSystem ID") @PathVariable Long id) {
        
        log.info("Deactivating GroupAgent for BusCoreSystem with ID: {}", id);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemService.getGroupAgentCoreBusSystemById(id)
                .orElseThrow(() -> new ApiException("GroupAgentCoreBusSystem not found with ID: " + id, HttpStatus.NOT_FOUND));
        
        // Verify it belongs to the specified GroupAgent
        if (!groupAgentCoreBusSystem.getGroupAgent().getId().equals(groupAgentId)) {
            throw new ApiException("GroupAgentCoreBusSystem does not belong to the specified GroupAgent", HttpStatus.BAD_REQUEST);
        }
        
        GroupAgentCoreBusSystem deactivatedGroupAgentCoreBusSystem = groupAgentCoreBusSystemService.deactivateGroupAgentForBusCoreSystem(groupAgentCoreBusSystem);
        GroupAgentCoreBusSystemResponseDto responseDto = mapToResponseDto(deactivatedGroupAgentCoreBusSystem);
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgent deactivated for BusCoreSystem successfully", responseDto));
    }

    /**
     * Get decrypted credentials for GroupAgentCoreBusSystem
     */
    @GetMapping("/uid/{uid}/decrypted-credentials")
    @Operation(summary = "Get decrypted credentials for GroupAgentCoreBusSystem")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Decrypted credentials retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgentCoreBusSystem not found")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentCoreBusSystemService.DecryptedGroupAgentCredentials>> getDecryptedCredentials(
            @Parameter(description = "GroupAgent UID") @PathVariable String groupAgentUid,
            @Parameter(description = "GroupAgentCoreBusSystem UID") @PathVariable String uid) {
        
        log.info("Getting decrypted credentials for GroupAgentCoreBusSystem with UID: {}", uid);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemService.getGroupAgentCoreBusSystemByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgentCoreBusSystem not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Verify it belongs to the specified GroupAgent
        if (!groupAgentCoreBusSystem.getGroupAgent().getUid().equals(groupAgentUid)) {
            throw new ApiException("GroupAgentCoreBusSystem does not belong to the specified GroupAgent", HttpStatus.BAD_REQUEST);
        }
        
        GroupAgentCoreBusSystemService.DecryptedGroupAgentCredentials credentials = groupAgentCoreBusSystemService
                .getDecryptedCredentials(groupAgentCoreBusSystem.getGroupAgent(), groupAgentCoreBusSystem.getBusCoreSystem())
                .orElseThrow(() -> new ApiException("Failed to retrieve decrypted credentials", HttpStatus.INTERNAL_SERVER_ERROR));
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Decrypted credentials retrieved successfully", credentials));
    }

    /**
     * Map GroupAgentCoreBusSystem entity to response DTO
     */
    private GroupAgentCoreBusSystemResponseDto mapToResponseDto(GroupAgentCoreBusSystem groupAgentCoreBusSystem) {
        return GroupAgentCoreBusSystemResponseDto.builder()
                .id(groupAgentCoreBusSystem.getId())
                .uid(groupAgentCoreBusSystem.getUid())
                .externalAgentIdentifier(groupAgentCoreBusSystem.getExternalAgentIdentifier())
                .username(groupAgentCoreBusSystem.getUsername())
                .isActive(groupAgentCoreBusSystem.isActive())
                .isPrimary(groupAgentCoreBusSystem.isPrimary())
                .externalSystemStatus(groupAgentCoreBusSystem.getExternalSystemStatus())
                .externalAgentId(groupAgentCoreBusSystem.getExternalAgentId())
                .configuration(groupAgentCoreBusSystem.getConfiguration())
                .endpointUrl(groupAgentCoreBusSystem.getEndpointUrl())
                .timeoutSeconds(groupAgentCoreBusSystem.getTimeoutSeconds())
                .retryAttempts(groupAgentCoreBusSystem.getRetryAttempts())
                .createdAt(groupAgentCoreBusSystem.getCreatedAt())
                .updatedAt(groupAgentCoreBusSystem.getUpdatedAt())
                .createdBy(groupAgentCoreBusSystem.getCreatedBy())
                .updatedBy(groupAgentCoreBusSystem.getUpdatedBy())
                .lastAuthenticationDate(groupAgentCoreBusSystem.getLastAuthenticationDate())
                .lastSyncDate(groupAgentCoreBusSystem.getLastSyncDate())
                .notes(groupAgentCoreBusSystem.getNotes())
                .busCoreSystemId(groupAgentCoreBusSystem.getBusCoreSystem().getId())
                .busCoreSystemUid(groupAgentCoreBusSystem.getBusCoreSystem().getUid())
                .busCoreSystemCode(groupAgentCoreBusSystem.getBusCoreSystem().getCode())
                .busCoreSystemName(groupAgentCoreBusSystem.getBusCoreSystem().getName())
                .busCoreSystemProviderName(groupAgentCoreBusSystem.getBusCoreSystem().getProviderName())
                .busCoreSystemBaseUrl(groupAgentCoreBusSystem.getBusCoreSystem().getBaseUrl())
                .groupAgentId(groupAgentCoreBusSystem.getGroupAgent().getId())
                .groupAgentUid(groupAgentCoreBusSystem.getGroupAgent().getUid())
                .groupAgentCode(groupAgentCoreBusSystem.getGroupAgent().getCode())
                .groupAgentName(groupAgentCoreBusSystem.getGroupAgent().getName())
                .partnerId(groupAgentCoreBusSystem.getGroupAgent().getPartner().getId())
                .partnerUid(groupAgentCoreBusSystem.getGroupAgent().getPartner().getUid())
                .partnerCode(groupAgentCoreBusSystem.getGroupAgent().getPartner().getCode())
                .partnerBusinessName(groupAgentCoreBusSystem.getGroupAgent().getPartner().getBusinessName())
                .build();
    }
}
