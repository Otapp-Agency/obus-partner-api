package com.obuspartners.api.admin;

import com.obuspartners.modules.agent_management.domain.dto.*;
import com.obuspartners.modules.agent_management.domain.entity.GroupAgent;
import com.obuspartners.modules.agent_management.service.GroupAgentService;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.common.util.PageResponseWrapper;
import com.obuspartners.modules.common.util.ResponseWrapper;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;
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

import java.util.stream.Collectors;

/**
 * REST Controller for Admin GroupAgent Management API endpoints
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/v1/group-agents")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Admin GroupAgent Management", description = "GroupAgent management operations for administrators")
public class AdminGroupAgentController {

    private final GroupAgentService groupAgentService;
    private final PartnerRepository partnerRepository;

    /**
     * Create a new GroupAgent
     */
    @PostMapping
    @Operation(summary = "Create a new GroupAgent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "GroupAgent created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "409", description = "GroupAgent code or external system identifier already exists")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentResponseDto>> createGroupAgent(
            @Valid @RequestBody CreateGroupAgentRequestDto request) {
        
        log.info("Creating new GroupAgent: {}", request.getName());
        
        // Validate partner exists
        Partner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ApiException("Partner not found with ID: " + request.getPartnerId(), HttpStatus.NOT_FOUND));
        
        // Create GroupAgent entity
        GroupAgent groupAgent = new GroupAgent();
        groupAgent.setPartner(partner);
        groupAgent.setCode(request.getCode());
        groupAgent.setName(request.getName());
        groupAgent.setDescription(request.getDescription());
        groupAgent.setExternalSystemIdentifier(request.getExternalSystemIdentifier());
        groupAgent.setType(request.getType());
        groupAgent.setContactPerson(request.getContactPerson());
        groupAgent.setContactEmail(request.getContactEmail());
        groupAgent.setContactPhone(request.getContactPhone());
        groupAgent.setBusinessName(request.getBusinessName());
        groupAgent.setBusinessAddress(request.getBusinessAddress());
        groupAgent.setTaxId(request.getTaxId());
        groupAgent.setLicenseNumber(request.getLicenseNumber());
        groupAgent.setNotes(request.getNotes());
        
        GroupAgent createdGroupAgent = groupAgentService.createGroupAgent(groupAgent);
        GroupAgentResponseDto responseDto = mapToResponseDto(createdGroupAgent);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseWrapper<>(true, 201, "GroupAgent created successfully", responseDto));
    }

    /**
     * Get GroupAgent by UID
     */
    @GetMapping("/uid/{uid}")
    @Operation(summary = "Get GroupAgent by UID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgent retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgent not found")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentResponseDto>> getGroupAgentByUid(
            @Parameter(description = "GroupAgent UID") @PathVariable String uid) {
        
        log.debug("Getting GroupAgent by UID: {}", uid);
        
        GroupAgent groupAgent = groupAgentService.getGroupAgentByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgent not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        GroupAgentResponseDto responseDto = mapToResponseDto(groupAgent);
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgent retrieved successfully", responseDto));
    }

    /**
     * Get all GroupAgents with pagination
     */
    @GetMapping
    @Operation(summary = "Get all GroupAgents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgents retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    })
    public ResponseEntity<PageResponseWrapper<GroupAgentResponseDto>> getAllGroupAgents(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.debug("Getting all GroupAgents with pagination: page={}, size={}, sortBy={}, sortDirection={}", 
                  page, size, sortBy, sortDirection);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<GroupAgent> groupAgentsPage = groupAgentService.getAllGroupAgents(pageable);
        Page<GroupAgentResponseDto> responseDtoPage = groupAgentsPage.map(this::mapToResponseDto);
        
        return ResponseEntity.ok(PageResponseWrapper.fromPage(responseDtoPage, "GroupAgents retrieved successfully"));
    }

    /**
     * Get all active GroupAgents for assignment (no pagination)
     */
    @GetMapping("/for-assignment")
    @Operation(summary = "Get all active GroupAgents for assignment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgents retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    })
    public ResponseEntity<ResponseWrapper<java.util.List<GroupAgentResponseDto>>> getGroupAgentsForAssignment() {
        
        log.debug("Getting all active GroupAgents for assignment");
        
        java.util.List<GroupAgent> groupAgents = groupAgentService.getAllActiveGroupAgents();
        java.util.List<GroupAgentResponseDto> responseDtos = groupAgents.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgents retrieved successfully", responseDtos));
    }

    /**
     * Update GroupAgent
     */
    @PutMapping("/uid/{uid}")
    @Operation(summary = "Update GroupAgent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgent updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgent not found"),
        @ApiResponse(responseCode = "409", description = "GroupAgent code or external system identifier already exists")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentResponseDto>> updateGroupAgent(
            @Parameter(description = "GroupAgent UID") @PathVariable String uid,
            @Valid @RequestBody UpdateGroupAgentRequestDto request) {
        
        log.info("Updating GroupAgent with UID: {}", uid);
        
        GroupAgent existingGroupAgent = groupAgentService.getGroupAgentByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgent not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Update fields
        if (request.getCode() != null) existingGroupAgent.setCode(request.getCode());
        if (request.getName() != null) existingGroupAgent.setName(request.getName());
        if (request.getDescription() != null) existingGroupAgent.setDescription(request.getDescription());
        if (request.getExternalSystemIdentifier() != null) existingGroupAgent.setExternalSystemIdentifier(request.getExternalSystemIdentifier());
        if (request.getType() != null) existingGroupAgent.setType(request.getType());
        if (request.getStatus() != null) existingGroupAgent.setStatus(request.getStatus());
        if (request.getContactPerson() != null) existingGroupAgent.setContactPerson(request.getContactPerson());
        if (request.getContactEmail() != null) existingGroupAgent.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) existingGroupAgent.setContactPhone(request.getContactPhone());
        if (request.getBusinessName() != null) existingGroupAgent.setBusinessName(request.getBusinessName());
        if (request.getBusinessAddress() != null) existingGroupAgent.setBusinessAddress(request.getBusinessAddress());
        if (request.getTaxId() != null) existingGroupAgent.setTaxId(request.getTaxId());
        if (request.getLicenseNumber() != null) existingGroupAgent.setLicenseNumber(request.getLicenseNumber());
        if (request.getNotes() != null) existingGroupAgent.setNotes(request.getNotes());
        
        GroupAgent updatedGroupAgent = groupAgentService.updateGroupAgent(existingGroupAgent);
        GroupAgentResponseDto responseDto = mapToResponseDto(updatedGroupAgent);
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgent updated successfully", responseDto));
    }

    /**
     * Delete GroupAgent
     */
    @DeleteMapping("/uid/{uid}")
    @Operation(summary = "Delete GroupAgent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgent deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgent not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete GroupAgent with associated agents or bus core systems")
    })
    public ResponseEntity<ResponseWrapper<String>> deleteGroupAgent(
            @Parameter(description = "GroupAgent UID") @PathVariable String uid) {
        
        log.info("Deleting GroupAgent with UID: {}", uid);
        
        GroupAgent groupAgent = groupAgentService.getGroupAgentByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgent not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        groupAgentService.deleteGroupAgent(groupAgent.getId());
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgent deleted successfully", "GroupAgent deleted successfully"));
    }

    /**
     * Get all GroupAgents by Partner with pagination
     */
    @GetMapping("/partner/{partnerId}")
    @Operation(summary = "Get all GroupAgents by Partner")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgents retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "Partner not found")
    })
    public ResponseEntity<PageResponseWrapper<GroupAgentResponseDto>> getGroupAgentsByPartner(
            @Parameter(description = "Partner ID") @PathVariable Long partnerId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.debug("Getting GroupAgents for partner ID: {}", partnerId);
        
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ApiException("Partner not found with ID: " + partnerId, HttpStatus.NOT_FOUND));
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<GroupAgent> groupAgentsPage = groupAgentService.getGroupAgentsByPartner(partner, pageable);
        Page<GroupAgentResponseDto> responseDtoPage = groupAgentsPage.map(this::mapToResponseDto);
        
        return ResponseEntity.ok(PageResponseWrapper.fromPage(responseDtoPage, "GroupAgents retrieved successfully"));
    }

    /**
     * Search GroupAgents
     */
    @PostMapping("/search")
    @Operation(summary = "Search GroupAgents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgents retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    })
    public ResponseEntity<PageResponseWrapper<GroupAgentResponseDto>> searchGroupAgents(
            @Valid @RequestBody GroupAgentSearchRequestDto searchRequest) {
        
        log.debug("Searching GroupAgents with criteria: {}", searchRequest);
        
        Sort sort = Sort.by(Sort.Direction.fromString(searchRequest.getSortDirection()), searchRequest.getSortBy());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
        
        Page<GroupAgent> groupAgentsPage;
        
        if (searchRequest.getPartnerId() != null) {
            Partner partner = partnerRepository.findById(searchRequest.getPartnerId())
                    .orElseThrow(() -> new ApiException("Partner not found with ID: " + searchRequest.getPartnerId(), HttpStatus.NOT_FOUND));
            
            if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
                groupAgentsPage = groupAgentService.searchGroupAgentsByPartner(partner, searchRequest.getSearchTerm(), pageable);
            } else {
                groupAgentsPage = groupAgentService.getGroupAgentsByPartner(partner, pageable);
            }
        } else {
            // For admin, search across all partners
            if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
                groupAgentsPage = groupAgentService.searchGroupAgents(searchRequest.getSearchTerm(), pageable);
            } else {
                groupAgentsPage = groupAgentService.getAllGroupAgents(pageable);
            }
        }
        
        Page<GroupAgentResponseDto> responseDtoPage = groupAgentsPage.map(this::mapToResponseDto);
        
        return ResponseEntity.ok(PageResponseWrapper.fromPage(responseDtoPage, "GroupAgents retrieved successfully"));
    }

    /**
     * Activate GroupAgent
     */
    @PostMapping("/uid/{uid}/activate")
    @Operation(summary = "Activate GroupAgent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgent activated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgent not found")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentResponseDto>> activateGroupAgent(
            @Parameter(description = "GroupAgent UID") @PathVariable String uid) {
        
        log.info("Activating GroupAgent with UID: {}", uid);
        
        GroupAgent groupAgent = groupAgentService.getGroupAgentByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgent not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        GroupAgent activatedGroupAgent = groupAgentService.activateGroupAgent(groupAgent);
        GroupAgentResponseDto responseDto = mapToResponseDto(activatedGroupAgent);
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgent activated successfully", responseDto));
    }

    /**
     * Suspend GroupAgent
     */
    @PostMapping("/uid/{uid}/suspend")
    @Operation(summary = "Suspend GroupAgent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgent suspended successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgent not found")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentResponseDto>> suspendGroupAgent(
            @Parameter(description = "GroupAgent UID") @PathVariable String uid) {
        
        log.info("Suspending GroupAgent with UID: {}", uid);
        
        GroupAgent groupAgent = groupAgentService.getGroupAgentByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgent not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        GroupAgent suspendedGroupAgent = groupAgentService.suspendGroupAgent(groupAgent);
        GroupAgentResponseDto responseDto = mapToResponseDto(suspendedGroupAgent);
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgent suspended successfully", responseDto));
    }

    /**
     * Deactivate GroupAgent
     */
    @PostMapping("/uid/{uid}/deactivate")
    @Operation(summary = "Deactivate GroupAgent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GroupAgent deactivated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required"),
        @ApiResponse(responseCode = "404", description = "GroupAgent not found")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentResponseDto>> deactivateGroupAgent(
            @Parameter(description = "GroupAgent UID") @PathVariable String uid) {
        
        log.info("Deactivating GroupAgent with UID: {}", uid);
        
        GroupAgent groupAgent = groupAgentService.getGroupAgentByUid(uid)
                .orElseThrow(() -> new ApiException("GroupAgent not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        GroupAgent deactivatedGroupAgent = groupAgentService.deactivateGroupAgent(groupAgent);
        GroupAgentResponseDto responseDto = mapToResponseDto(deactivatedGroupAgent);
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "GroupAgent deactivated successfully", responseDto));
    }

    /**
     * Get GroupAgent statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get GroupAgent statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin access required")
    })
    public ResponseEntity<ResponseWrapper<GroupAgentStatsDto>> getGroupAgentStats() {
        
        log.debug("Getting GroupAgent statistics");
        
        // This would need to be implemented in the service layer
        // For now, return empty stats
        GroupAgentStatsDto stats = GroupAgentStatsDto.builder()
                .totalGroupAgents(0)
                .activeGroupAgents(0)
                .suspendedGroupAgents(0)
                .inactiveGroupAgents(0)
                .build();
        
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Statistics retrieved successfully", stats));
    }

    /**
     * Map GroupAgent entity to response DTO
     */
    private GroupAgentResponseDto mapToResponseDto(GroupAgent groupAgent) {
        return GroupAgentResponseDto.builder()
                .id(groupAgent.getId())
                .uid(groupAgent.getUid())
                .code(groupAgent.getCode())
                .name(groupAgent.getName())
                .description(groupAgent.getDescription())
                .externalSystemIdentifier(groupAgent.getExternalSystemIdentifier())
                .type(groupAgent.getType())
                .status(groupAgent.getStatus())
                .contactPerson(groupAgent.getContactPerson())
                .contactEmail(groupAgent.getContactEmail())
                .contactPhone(groupAgent.getContactPhone())
                .businessName(groupAgent.getBusinessName())
                .businessAddress(groupAgent.getBusinessAddress())
                .taxId(groupAgent.getTaxId())
                .licenseNumber(groupAgent.getLicenseNumber())
                .createdAt(groupAgent.getCreatedAt())
                .updatedAt(groupAgent.getUpdatedAt())
                .activatedAt(groupAgent.getActivatedAt())
                .lastActivityDate(groupAgent.getLastActivityDate())
                .notes(groupAgent.getNotes())
                .partnerId(groupAgent.getPartner().getId())
                .partnerUid(groupAgent.getPartner().getUid())
                .partnerCode(groupAgent.getPartner().getCode())
                .partnerBusinessName(groupAgent.getPartner().getBusinessName())
                .agentCount(groupAgent.getAgentCount())
                .busCoreSystemCount(groupAgent.getCoreBusSystems().size())
                .activeBusCoreSystemCount((int) groupAgent.getCoreBusSystems().stream().filter(cbs -> cbs.isActive()).count())
                .busCoreSystems(groupAgent.getCoreBusSystems().stream()
                        .map(this::mapToSummaryDto)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Map GroupAgentCoreBusSystem entity to summary DTO
     */
    private GroupAgentCoreBusSystemSummaryDto mapToSummaryDto(com.obuspartners.modules.agent_management.domain.entity.GroupAgentCoreBusSystem coreBusSystem) {
        return GroupAgentCoreBusSystemSummaryDto.builder()
                .id(coreBusSystem.getId())
                .uid(coreBusSystem.getUid())
                .externalAgentIdentifier(coreBusSystem.getExternalAgentIdentifier())
                .username(coreBusSystem.getUsername())
                .isActive(coreBusSystem.isActive())
                .isPrimary(coreBusSystem.isPrimary())
                .externalSystemStatus(coreBusSystem.getExternalSystemStatus())
                .busCoreSystemId(coreBusSystem.getBusCoreSystem().getId())
                .busCoreSystemCode(coreBusSystem.getBusCoreSystem().getCode())
                .busCoreSystemName(coreBusSystem.getBusCoreSystem().getName())
                .busCoreSystemProviderName(coreBusSystem.getBusCoreSystem().getProviderName())
                .lastAuthenticationDate(coreBusSystem.getLastAuthenticationDate())
                .lastSyncDate(coreBusSystem.getLastSyncDate())
                .build();
    }
}
