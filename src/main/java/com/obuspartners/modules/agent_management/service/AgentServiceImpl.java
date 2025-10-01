package com.obuspartners.modules.agent_management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.agent_management.domain.dto.*;
import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.service.AgentRequestService;
import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;
import com.obuspartners.modules.agent_management.repository.AgentRepository;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;
import com.obuspartners.modules.common.service.EmailService;
import com.obuspartners.modules.common.service.EventProducerService;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserType;
import com.obuspartners.modules.user_and_role_management.service.UserService;
import com.obuspartners.modules.common.util.PasswordHelperService;

/**
 * Implementation of AgentService for managing agent operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentServiceImpl implements AgentService {

    private final AgentRepository agentRepository;
    private final PartnerRepository partnerRepository;
    private final EmailService emailService;
    private final EventProducerService eventProducerService;
    private final AgentRequestService agentRequestService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    // CRUD Operations

    @Override
    @Transactional
    public AgentResponseDto createAgent(CreateAgentRequestDto createRequest) {
        log.info("Creating new agent request with partner agent number: {}", createRequest.getPartnerAgentNumber());

        // Delegate to AgentRequestService to create the request
        // This will create an AgentRequest, PartnerAgentVerification, and send Kafka event
        var agentRequestResponse = agentRequestService.createAgentRequest(createRequest);
        
        log.info("Agent request created successfully with UID: {}", agentRequestResponse.getUid());
        
        // Return a response indicating the request was created and is pending verification
        // The actual Agent will be created after successful verification
        return AgentResponseDto.builder()
                .uid(agentRequestResponse.getUid()) // Use AgentRequest UID for now
                .partnerId(agentRequestResponse.getPartnerId())
                .partnerUid(null) // Partner UID not available for agent requests
                .partnerCode(agentRequestResponse.getPartnerCode())
                .partnerBusinessName(agentRequestResponse.getPartnerBusinessName())
                .superAgentId(agentRequestResponse.getSuperAgentId())
                .superAgentCode(agentRequestResponse.getSuperAgentCode())
                .partnerAgentNumber(agentRequestResponse.getPartnerAgentNumber())
                .businessName(agentRequestResponse.getBusinessName())
                .contactPerson(agentRequestResponse.getContactPerson())
                .phoneNumber(agentRequestResponse.getPhoneNumber())
                .msisdn(agentRequestResponse.getMsisdn())
                .businessEmail(agentRequestResponse.getBusinessEmail())
                .businessAddress(agentRequestResponse.getBusinessAddress())
                .taxId(agentRequestResponse.getTaxId())
                .licenseNumber(agentRequestResponse.getLicenseNumber())
                .agentType(agentRequestResponse.getAgentType())
                .notes(agentRequestResponse.getNotes())
                .status(AgentStatus.PENDING_APPROVAL) // Indicate it's pending approval
                .registrationDate(agentRequestResponse.getRequestedAt())
                .code(null) // Will be generated after verification
                .passName(null) // Will be generated after verification
                .passCode(null) // Will be generated after verification
                .createdAt(agentRequestResponse.getCreatedAt())
                .updatedAt(agentRequestResponse.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public AgentResponseDto createSuperAgent(CreateSuperAgentRequestDto createRequest) {
        log.info("Creating new super agent with partner agent number: {}", createRequest.getPartnerAgentNumber());

        // Validate partner exists
        Partner partner = partnerRepository.findById(createRequest.getPartnerId())
                .orElseThrow(() -> new ApiException("Partner not found with ID: " + createRequest.getPartnerId(), HttpStatus.NOT_FOUND));

        // Validate username uniqueness
        if (userService.existsByUsername(createRequest.getUsername())) {
            throw new ApiException("Username already exists: " + createRequest.getUsername(), HttpStatus.CONFLICT);
        }

        // Validate email uniqueness
        if (userService.existsByEmail(createRequest.getEmail())) {
            throw new ApiException("Email already exists: " + createRequest.getEmail(), HttpStatus.CONFLICT);
        }

        // Validate agent number uniqueness within partner
        if (agentRepository.existsByPartnerAndPartnerAgentNumber(partner, createRequest.getPartnerAgentNumber())) {
            throw new ApiException("Agent number already exists for this partner: " + createRequest.getPartnerAgentNumber(), HttpStatus.CONFLICT);
        }

        // Create Agent entity
        Agent agent = new Agent();
        agent.setPartnerAgentNumber(createRequest.getPartnerAgentNumber());
        agent.setBusinessName(createRequest.getBusinessName());
        agent.setContactPerson(createRequest.getContactPerson());
        agent.setPhoneNumber(createRequest.getPhoneNumber());
        agent.setMsisdn(createRequest.getMsisdn());
        agent.setBusinessEmail(createRequest.getBusinessEmail());
        agent.setBusinessAddress(createRequest.getBusinessAddress());
        agent.setTaxId(createRequest.getTaxId());
        agent.setLicenseNumber(createRequest.getLicenseNumber());
        agent.setAgentType(AgentType.SUPER_AGENT);
        agent.setPartner(partner);
        agent.setNotes(createRequest.getNotes());
        agent.setStatus(AgentStatus.ACTIVE);
        agent.setRegistrationDate(LocalDateTime.now());

        // Generate agent code and credentials
        agent.setCode(generateAgentCodeWithPartner(partner.getCode(), 4));
        String agentNumber = createRequest.getPartnerAgentNumber();
        String plainPassName = generateAgentLoginUsername(partner.getCode(), createRequest.getPartnerAgentNumber()); // Use consistent format
        String plainPassCode = generateAgentPasscode(6);

        agent.setPassName(plainPassName);
        agent.setPassCode(passwordEncoder.encode(plainPassCode));

        // Save Agent first
        Agent savedAgent = agentRepository.save(agent);
        log.info("Super agent created successfully with UID: {}", savedAgent.getUid());

        // Create User account for dashboard login
        String dashboardPassword = PasswordHelperService.generateStrongPassword(); // Generate strong password for dashboard
        User user = new User();
        user.setUsername(createRequest.getUsername());
        user.setEmail(createRequest.getEmail());
        user.setPassword(dashboardPassword); // Set the generated password
        user.setDisplayName(createRequest.getDisplayName());
        user.setUserType(UserType.AGENT);
        user.setAgent(savedAgent); // Link to the agent
        user.setPartner(partner); // Set partner for context
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setRequirePasswordChange(true); // Force password change on first login
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userService.save(user);
        log.info("User account created successfully for super agent with ID: {}", savedUser.getId());

        // Send email notification with credentials
        try {
            String subject = "Super Agent Account Created - " + partner.getBusinessName();
            String body = String.format(
                "Dear %s,\n\n" +
                "Your Super Agent account has been successfully created with %s.\n\n" +
                "Account Details:\n" +
                "• Agent Code: %s\n" +
                "• Business Name: %s\n" +
                "• Partner: %s\n\n" +
                "Dashboard Login Credentials:\n" +
                "• Username: %s\n" +
                "• Password: %s\n\n" +
                "Agent Login Credentials (for App access):\n" +
                "• Pass Name: %s\n" +
                "• Pass Code: %s\n\n" +
                "Please keep these credentials secure and do not share them with unauthorized persons.\n\n" +
                "If you have any questions, please contact your system administrator.\n\n" +
                "Best regards,\n" +
                "OBUS Partners Team",
                createRequest.getDisplayName(),
                partner.getBusinessName(),
                agent.getCode(),
                createRequest.getDisplayName(),
                partner.getBusinessName(),
                createRequest.getUsername(),
                dashboardPassword,
                agentNumber,
                plainPassCode
            );
            
            emailService.sendEmail(createRequest.getEmail(), subject, body);
            log.info("Super agent creation notification sent successfully");
        } catch (Exception e) {
            log.warn("Failed to send super agent creation notification: {}", e.getMessage());
            // Don't fail the creation if email fails
        }

        return mapToAgentResponseDto(savedAgent);
    }

    @Override
    @Transactional
    public AgentResponseDto updateAgent(String uid, UpdateAgentRequestDto updateRequest) {
        log.info("Updating agent with UID: {}", uid);

        Agent agent = agentRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent not found with UID: " + uid, HttpStatus.NOT_FOUND));

        // Update fields if provided
        if (StringUtils.hasText(updateRequest.getBusinessName())) {
            agent.setBusinessName(updateRequest.getBusinessName());
        }
        if (StringUtils.hasText(updateRequest.getContactPerson())) {
            agent.setContactPerson(updateRequest.getContactPerson());
        }
        if (StringUtils.hasText(updateRequest.getPhoneNumber())) {
            // Validate phone number uniqueness if changed
            if (!updateRequest.getPhoneNumber().equals(agent.getPhoneNumber()) &&
                agentRepository.existsByPhoneNumber(updateRequest.getPhoneNumber())) {
                throw new ApiException("Phone number already exists: " + updateRequest.getPhoneNumber(), HttpStatus.CONFLICT);
            }
            agent.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (StringUtils.hasText(updateRequest.getBusinessEmail())) {
            // Validate email uniqueness if changed
            if (!updateRequest.getBusinessEmail().equals(agent.getBusinessEmail()) &&
                agentRepository.existsByBusinessEmail(updateRequest.getBusinessEmail())) {
                throw new ApiException("Business email already exists: " + updateRequest.getBusinessEmail(), HttpStatus.CONFLICT);
            }
            agent.setBusinessEmail(updateRequest.getBusinessEmail());
        }
        if (StringUtils.hasText(updateRequest.getBusinessAddress())) {
            agent.setBusinessAddress(updateRequest.getBusinessAddress());
        }
        if (StringUtils.hasText(updateRequest.getTaxId())) {
            // Validate tax ID uniqueness if changed
            if (!updateRequest.getTaxId().equals(agent.getTaxId()) &&
                agentRepository.existsByTaxId(updateRequest.getTaxId())) {
                throw new ApiException("Tax ID already exists: " + updateRequest.getTaxId(), HttpStatus.CONFLICT);
            }
            agent.setTaxId(updateRequest.getTaxId());
        }
        if (StringUtils.hasText(updateRequest.getLicenseNumber())) {
            // Validate license number uniqueness if changed
            if (!updateRequest.getLicenseNumber().equals(agent.getLicenseNumber()) &&
                agentRepository.existsByLicenseNumber(updateRequest.getLicenseNumber())) {
                throw new ApiException("License number already exists: " + updateRequest.getLicenseNumber(), HttpStatus.CONFLICT);
            }
            agent.setLicenseNumber(updateRequest.getLicenseNumber());
        }
        if (updateRequest.getAgentType() != null) {
            agent.setAgentType(updateRequest.getAgentType());
        }
        if (updateRequest.getStatus() != null) {
            agent.setStatus(updateRequest.getStatus());
        }
        if (updateRequest.getSuperAgentId() != null) {
            Agent superAgent = agentRepository.findById(updateRequest.getSuperAgentId())
                    .orElseThrow(() -> new ApiException("Super agent not found with ID: " + updateRequest.getSuperAgentId(), HttpStatus.NOT_FOUND));
            
            validateSuperAgentAssignmentInternal(agent.getAgentType(), superAgent);
            agent.setSuperAgent(superAgent);
        }
        if (StringUtils.hasText(updateRequest.getNotes())) {
            agent.setNotes(updateRequest.getNotes());
        }

        Agent updatedAgent = agentRepository.save(agent);
        log.info("Agent updated successfully with UID: {}", updatedAgent.getUid());

        return mapToAgentResponseDto(updatedAgent);
    }

    @Override
    @Transactional
    public AgentResponseDto updateAgentByCode(String agentCode, UpdateAgentRequestDto updateRequest) {
        log.info("Updating agent with code: {}", agentCode);

        Agent agent = agentRepository.findByCode(agentCode)
                .orElseThrow(() -> new ApiException("Agent not found with code: " + agentCode, HttpStatus.NOT_FOUND));

        return updateAgent(agent.getUid(), updateRequest);
    }

    // Retrieve Operations

    @Override
    public Optional<AgentResponseDto> getAgent(String uid) {
        log.debug("Retrieving agent with UID: {}", uid);
        return agentRepository.findByUid(uid).map(this::mapToAgentResponseDto);
    }

    @Override
    public Optional<AgentResponseDto> getAgentById(Long agentId) {
        log.debug("Retrieving agent with ID: {}", agentId);
        return agentRepository.findById(agentId).map(this::mapToAgentResponseDto);
    }

    @Override
    public Optional<AgentResponseDto> getAgentByCode(String agentCode) {
        log.debug("Retrieving agent with code: {}", agentCode);
        return agentRepository.findByCode(agentCode).map(this::mapToAgentResponseDto);
    }

    @Override
    public Optional<AgentResponseDto> getAgentByBusinessEmail(String businessEmail) {
        log.debug("Retrieving agent with business email: {}", businessEmail);
        return agentRepository.findByBusinessEmail(businessEmail).map(this::mapToAgentResponseDto);
    }

    @Override
    public Optional<AgentResponseDto> getAgentByPhoneNumber(String phoneNumber) {
        log.debug("Retrieving agent with phone number: {}", phoneNumber);
        return agentRepository.findByPhoneNumber(phoneNumber).map(this::mapToAgentResponseDto);
    }

    // Status Management

    @Override
    @Transactional
    public AgentResponseDto approveAgent(String uid) {
        log.info("Approving agent with UID: {}", uid);

        Agent agent = agentRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent not found with UID: " + uid, HttpStatus.NOT_FOUND));

        if (agent.getStatus() != AgentStatus.PENDING_APPROVAL) {
            throw new ApiException("Agent can only be approved from PENDING_APPROVAL status. Current status: " + agent.getStatus(), HttpStatus.BAD_REQUEST);
        }

        agent.approve();
        Agent approvedAgent = agentRepository.save(agent);

        log.info("Agent approved successfully with UID: {}", approvedAgent.getUid());
        return mapToAgentResponseDto(approvedAgent);
    }

    @Override
    @Transactional
    public AgentResponseDto approveAgentByCode(String agentCode) {
        log.info("Approving agent with code: {}", agentCode);

        Agent agent = agentRepository.findByCode(agentCode)
                .orElseThrow(() -> new ApiException("Agent not found with code: " + agentCode, HttpStatus.NOT_FOUND));

        return approveAgent(agent.getUid());
    }

    @Override
    @Transactional
    public AgentResponseDto rejectAgent(String uid) {
        log.info("Rejecting agent with UID: {}", uid);

        Agent agent = agentRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent not found with UID: " + uid, HttpStatus.NOT_FOUND));

        if (agent.getStatus() != AgentStatus.PENDING_APPROVAL) {
            throw new ApiException("Agent can only be rejected from PENDING_APPROVAL status. Current status: " + agent.getStatus(), HttpStatus.BAD_REQUEST);
        }

        agent.setStatus(AgentStatus.REJECTED);
        agent.setUpdatedAt(LocalDateTime.now());
        Agent rejectedAgent = agentRepository.save(agent);

        log.info("Agent rejected successfully with UID: {}", rejectedAgent.getUid());
        return mapToAgentResponseDto(rejectedAgent);
    }

    @Override
    @Transactional
    public AgentResponseDto suspendAgent(String uid) {
        log.info("Suspending agent with UID: {}", uid);

        Agent agent = agentRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent not found with UID: " + uid, HttpStatus.NOT_FOUND));

        agent.suspend();
        Agent suspendedAgent = agentRepository.save(agent);

        log.info("Agent suspended successfully with UID: {}", suspendedAgent.getUid());
        return mapToAgentResponseDto(suspendedAgent);
    }

    @Override
    @Transactional
    public AgentResponseDto activateAgent(String uid) {
        log.info("Activating agent with UID: {}", uid);

        Agent agent = agentRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent not found with UID: " + uid, HttpStatus.NOT_FOUND));

        agent.activate();
        Agent activatedAgent = agentRepository.save(agent);

        log.info("Agent activated successfully with UID: {}", activatedAgent.getUid());
        return mapToAgentResponseDto(activatedAgent);
    }

    @Override
    @Transactional
    public AgentResponseDto deactivateAgent(String uid) {
        log.info("Deactivating agent with UID: {}", uid);

        Agent agent = agentRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent not found with UID: " + uid, HttpStatus.NOT_FOUND));

        agent.deactivate();
        Agent deactivatedAgent = agentRepository.save(agent);

        log.info("Agent deactivated successfully with UID: {}", deactivatedAgent.getUid());
        return mapToAgentResponseDto(deactivatedAgent);
    }

    @Override
    @Transactional
    public AgentResponseDto lockAgent(String uid) {
        log.info("Locking agent with UID: {}", uid);

        Agent agent = agentRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent not found with UID: " + uid, HttpStatus.NOT_FOUND));

        agent.setStatus(AgentStatus.LOCKED);
        agent.setUpdatedAt(LocalDateTime.now());
        Agent lockedAgent = agentRepository.save(agent);

        log.info("Agent locked successfully with UID: {}", lockedAgent.getUid());
        return mapToAgentResponseDto(lockedAgent);
    }

    @Override
    @Transactional
    public AgentResponseDto updateAgentStatus(String uid, AgentStatus status) {
        log.info("Updating agent status with UID: {} to status: {}", uid, status);

        Agent agent = agentRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent not found with UID: " + uid, HttpStatus.NOT_FOUND));

        agent.setStatus(status);
        if (status == AgentStatus.ACTIVE && agent.getApprovalDate() == null) {
            agent.setApprovalDate(LocalDateTime.now());
        }
        agent.setUpdatedAt(LocalDateTime.now());
        
        Agent updatedAgent = agentRepository.save(agent);

        log.info("Agent status updated successfully with UID: {}", updatedAgent.getUid());
        return mapToAgentResponseDto(updatedAgent);
    }

    // Agent Hierarchy Management

    @Override
    @Transactional
    public AgentResponseDto assignSuperAgent(String subAgentUid, String superAgentUid) {
        log.info("Assigning super agent {} to sub-agent {}", superAgentUid, subAgentUid);

        Agent subAgent = agentRepository.findByUid(subAgentUid)
                .orElseThrow(() -> new ApiException("Sub-agent not found with UID: " + subAgentUid, HttpStatus.NOT_FOUND));

        Agent superAgent = agentRepository.findByUid(superAgentUid)
                .orElseThrow(() -> new ApiException("Super agent not found with UID: " + superAgentUid, HttpStatus.NOT_FOUND));

        // Validate assignment
        if (!validateSuperAgentAssignment(subAgentUid, superAgentUid)) {
            throw new ApiException("Invalid super agent assignment", HttpStatus.BAD_REQUEST);
        }

        subAgent.assignSuperAgent(superAgent);
        Agent updatedSubAgent = agentRepository.save(subAgent);

        log.info("Super agent assigned successfully");
        return mapToAgentResponseDto(updatedSubAgent);
    }

    @Override
    @Transactional
    public AgentResponseDto removeSuperAgent(String subAgentUid) {
        log.info("Removing super agent from sub-agent {}", subAgentUid);

        Agent subAgent = agentRepository.findByUid(subAgentUid)
                .orElseThrow(() -> new ApiException("Sub-agent not found with UID: " + subAgentUid, HttpStatus.NOT_FOUND));

        subAgent.setSuperAgent(null);
        subAgent.setUpdatedAt(LocalDateTime.now());
        Agent updatedSubAgent = agentRepository.save(subAgent);

        log.info("Super agent removed successfully");
        return mapToAgentResponseDto(updatedSubAgent);
    }

    @Override
    public Page<AgentSummaryDto> getSubAgents(String superAgentUid, Pageable pageable) {
        log.debug("Retrieving sub-agents for super agent UID: {}", superAgentUid);

        Agent superAgent = agentRepository.findByUid(superAgentUid)
                .orElseThrow(() -> new ApiException("Super agent not found with UID: " + superAgentUid, HttpStatus.NOT_FOUND));

        return agentRepository.findBySuperAgent(superAgent, pageable)
                .map(this::mapToAgentSummaryDto);
    }

    @Override
    public List<AgentResponseDto> getAgentHierarchy(String superAgentUid) {
        log.debug("Retrieving agent hierarchy for super agent UID: {}", superAgentUid);

        Agent superAgent = agentRepository.findByUid(superAgentUid)
                .orElseThrow(() -> new ApiException("Super agent not found with UID: " + superAgentUid, HttpStatus.NOT_FOUND));

        return agentRepository.findAgentHierarchy(superAgent)
                .stream()
                .map(this::mapToAgentResponseDto)
                .collect(Collectors.toList());
    }

    // Partner-related Operations

    @Override
    public Page<AgentSummaryDto> getAgentsByPartner(String partnerUid, Pageable pageable) {
        log.debug("Retrieving agents for partner UID: {}", partnerUid);

        Partner partner = partnerRepository.findByUid(partnerUid)
                .orElseThrow(() -> new ApiException("Partner not found with UID: " + partnerUid, HttpStatus.NOT_FOUND));

        return agentRepository.findByPartner(partner, pageable)
                .map(this::mapToAgentSummaryDto);
    }

    @Override
    public Page<AgentSummaryDto> getActiveAgentsByPartner(String partnerUid, Pageable pageable) {
        log.debug("Retrieving active agents for partner UID: {}", partnerUid);

        Partner partner = partnerRepository.findByUid(partnerUid)
                .orElseThrow(() -> new ApiException("Partner not found with UID: " + partnerUid, HttpStatus.NOT_FOUND));

        return agentRepository.findByPartnerAndStatus(partner, AgentStatus.ACTIVE, pageable)
                .map(this::mapToAgentSummaryDto);
    }

    @Override
    public Page<AgentSummaryDto> getSuperAgentsByPartner(String partnerUid, Pageable pageable) {
        log.debug("Retrieving super agents for partner UID: {}", partnerUid);

        Partner partner = partnerRepository.findByUid(partnerUid)
                .orElseThrow(() -> new ApiException("Partner not found with UID: " + partnerUid, HttpStatus.NOT_FOUND));

        return agentRepository.findSuperAgentsByPartner(partner, pageable)
                .map(this::mapToAgentSummaryDto);
    }

    @Override
    public long countAgentsByPartner(String partnerUid) {
        log.debug("Counting agents for partner UID: {}", partnerUid);

        Partner partner = partnerRepository.findByUid(partnerUid)
                .orElseThrow(() -> new ApiException("Partner not found with UID: " + partnerUid, HttpStatus.NOT_FOUND));

        return agentRepository.countByPartner(partner);
    }

    // Query Operations

    @Override
    public Page<AgentSummaryDto> getAllAgentSummaries(Pageable pageable) {
        log.debug("Retrieving all agents with pagination");
        return agentRepository.findAll(pageable).map(this::mapToAgentSummaryDto);
    }

    @Override
    public List<AgentSummaryDto> getAllAgentsForAssignment() {
        log.debug("Retrieving all agents for assignment");
        return agentRepository.findAll().stream()
                .map(this::mapToAgentSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AgentSummaryDto> getAgentsByStatus(AgentStatus status, Pageable pageable) {
        log.debug("Retrieving agents by status: {}", status);
        return agentRepository.findByStatus(status, pageable).map(this::mapToAgentSummaryDto);
    }

    @Override
    public Page<AgentSummaryDto> getAgentsByType(AgentType agentType, Pageable pageable) {
        log.debug("Retrieving agents by type: {}", agentType);
        return agentRepository.findByAgentType(agentType, pageable).map(this::mapToAgentSummaryDto);
    }

    @Override
    public Page<AgentSummaryDto> getActiveAgents(Pageable pageable) {
        log.debug("Retrieving active agents");
        return agentRepository.findActiveAgents(pageable).map(this::mapToAgentSummaryDto);
    }

    @Override
    public Page<AgentSummaryDto> getPendingAgents(Pageable pageable) {
        log.debug("Retrieving pending agents");
        return agentRepository.findPendingAgents(pageable).map(this::mapToAgentSummaryDto);
    }

    @Override
    public Page<AgentSummaryDto> searchAgentsByBusinessName(String businessName, Pageable pageable) {
        log.debug("Searching agents by business name: {}", businessName);
        return agentRepository.findByBusinessNameContainingIgnoreCase(businessName, pageable)
                .map(this::mapToAgentSummaryDto);
    }

    @Override
    public Page<AgentSummaryDto> searchAgentsByContactPerson(String contactPerson, Pageable pageable) {
        log.debug("Searching agents by contact person: {}", contactPerson);
        return agentRepository.findByContactPersonContainingIgnoreCase(contactPerson, pageable)
                .map(this::mapToAgentSummaryDto);
    }

    @Override
    public Page<AgentSummaryDto> searchAgents(AgentSearchRequestDto searchRequest) {
        log.debug("Searching agents with criteria: {}", searchRequest);
        
        Long partnerId = null;
        if (StringUtils.hasText(searchRequest.getBusinessName())) {
            // If partner UID is provided in search, convert to ID
            // This would need to be handled based on your search criteria structure
        }

        return agentRepository.searchAgents(
                partnerId,
                searchRequest.getBusinessName(),
                searchRequest.getStatus(),
                searchRequest.getAgentType(),
                searchRequest.getPageable()
        ).map(this::mapToAgentSummaryDto);
    }

    @Override
    public Page<AgentSummaryDto> getAgentsByRegistrationDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Retrieving agents by registration date range: {} to {}", startDate, endDate);
        return agentRepository.findByRegistrationDateBetween(startDate, endDate, pageable)
                .map(this::mapToAgentSummaryDto);
    }

    @Override
    public Page<AgentSummaryDto> getAgentsByLastActivityDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Retrieving agents by last activity date range: {} to {}", startDate, endDate);
        return agentRepository.findByLastActivityDateBetween(startDate, endDate, pageable)
                .map(this::mapToAgentSummaryDto);
    }

    // Validation and Existence Checks

    @Override
    public boolean existsByUid(String uid) {
        return agentRepository.existsByUid(uid);
    }

    @Override
    public boolean existsByCode(String agentCode) {
        return agentRepository.existsByCode(agentCode);
    }

    @Override
    public boolean existsByBusinessEmail(String businessEmail) {
        return agentRepository.existsByBusinessEmail(businessEmail);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return agentRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public boolean existsByTaxId(String taxId) {
        return agentRepository.existsByTaxId(taxId);
    }

    @Override
    public boolean existsByLicenseNumber(String licenseNumber) {
        return agentRepository.existsByLicenseNumber(licenseNumber);
    }

    @Override
    public boolean validateAgent(Agent agent) {
        // Implement validation logic
        if (!StringUtils.hasText(agent.getCode())) {
            return false;
        }
        if (agent.getAgentType() == null) {
            return false;
        }
        if (agent.getPartner() == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean validateSuperAgentAssignment(String subAgentUid, String superAgentUid) {
        // Validate that the assignment is valid
        Agent subAgent = agentRepository.findByUid(subAgentUid).orElse(null);
        Agent superAgent = agentRepository.findByUid(superAgentUid).orElse(null);

        if (subAgent == null || superAgent == null) {
            return false;
        }

        return validateSuperAgentAssignmentInternal(subAgent.getAgentType(), superAgent);
    }

    // Utility Methods

    @Override
    public String generateAgentCode(String partnerUid) {
        Partner partner = partnerRepository.findByUid(partnerUid)
                .orElseThrow(() -> new ApiException("Partner not found with UID: " + partnerUid, HttpStatus.NOT_FOUND));

        // Generate agent code with partner code + random 4-digit number
        return generateAgentCodeWithPartner(partner.getCode(), 4);
    }

    @Override
    public String generateAgentLoginUsername(String partnerCode, String partnerAgentNumber) {
        return partnerCode + "-" + partnerAgentNumber;
    }

    @Override
    public String generateAgentPasscode(int digits) {
        if (digits <= 0) {
            throw new ApiException("Number of digits must be positive", HttpStatus.BAD_REQUEST);
        }
        
        Random random = new Random();
        int firstDigit = random.nextInt(9) + 1; // 1-9 (doesn't start with 0)
        
        if (digits == 1) {
            return String.valueOf(firstDigit);
        }
        
        // Calculate the maximum value for remaining digits
        int maxRemaining = (int) Math.pow(10, digits - 1) - 1; // e.g., for 6 digits: 99999
        int remainingDigits = random.nextInt(maxRemaining + 1); // 0 to maxRemaining
        
        // Format with leading zeros if needed
        String formatString = "%d%0" + (digits - 1) + "d";
        return String.format(formatString, firstDigit, remainingDigits);
    }

    @Override
    @Transactional
    public void updateLastActivity(String uid) {
        Agent agent = agentRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent not found with UID: " + uid, HttpStatus.NOT_FOUND));

        agent.updateLastActivity();
        agentRepository.save(agent);
    }

    // Statistics and Counting

    @Override
    public long countAgentsByStatus(AgentStatus status) {
        return agentRepository.countByStatus(status);
    }

    @Override
    public long countAgentsByType(AgentType agentType) {
        return agentRepository.countByAgentType(agentType);
    }

    @Override
    public long countActiveAgents() {
        return agentRepository.countByStatus(AgentStatus.ACTIVE);
    }

    @Override
    public long countPendingAgents() {
        return agentRepository.countByStatus(AgentStatus.PENDING_APPROVAL);
    }

    @Override
    public long countSubAgents(String superAgentUid) {
        Agent superAgent = agentRepository.findByUid(superAgentUid)
                .orElseThrow(() -> new ApiException("Super agent not found with UID: " + superAgentUid, HttpStatus.NOT_FOUND));

        return agentRepository.countBySuperAgent(superAgent);
    }

    @Override
    public AgentStatistics getAgentStatistics() {
        log.debug("Generating agent statistics");

        long totalAgents = agentRepository.count();
        long activeAgents = countAgentsByStatus(AgentStatus.ACTIVE);
        long pendingAgents = countAgentsByStatus(AgentStatus.PENDING_APPROVAL);
        long suspendedAgents = countAgentsByStatus(AgentStatus.SUSPENDED);
        long rejectedAgents = countAgentsByStatus(AgentStatus.REJECTED);
        long superAgents = countAgentsByType(AgentType.SUPER_AGENT);
        long subAgents = countAgentsByType(AgentType.SUB_AGENT);

        return new AgentStatistics(
                totalAgents, activeAgents, pendingAgents,
                suspendedAgents, rejectedAgents, superAgents,
                subAgents, 0 // agentsByPartner - would need partner context
        );
    }

    @Override
    public AgentStatistics getAgentStatisticsByPartner(String partnerUid) {
        log.debug("Generating agent statistics for partner UID: {}", partnerUid);

        Partner partner = partnerRepository.findByUid(partnerUid)
                .orElseThrow(() -> new ApiException("Partner not found with UID: " + partnerUid, HttpStatus.NOT_FOUND));

        long totalAgents = agentRepository.countByPartner(partner);
        long activeAgents = agentRepository.countByPartnerAndStatus(partner, AgentStatus.ACTIVE);
        long pendingAgents = agentRepository.countByPartnerAndStatus(partner, AgentStatus.PENDING_APPROVAL);
        long suspendedAgents = agentRepository.countByPartnerAndStatus(partner, AgentStatus.SUSPENDED);
        long rejectedAgents = agentRepository.countByPartnerAndStatus(partner, AgentStatus.REJECTED);
        
        // For simplicity, using total counts for super/sub agents by partner
        // In practice, you might want specific queries for these
        long superAgents = agentRepository.findByPartnerAndAgentType(partner, AgentType.SUPER_AGENT, PageRequest.of(0, 1)).getTotalElements();
        long subAgents = agentRepository.findByPartnerAndAgentType(partner, AgentType.SUB_AGENT, PageRequest.of(0, 1)).getTotalElements();

        return new AgentStatistics(
                totalAgents, activeAgents, pendingAgents,
                suspendedAgents, rejectedAgents, superAgents,
                subAgents, totalAgents
        );
    }

    // Bulk Operations

    @Override
    @Transactional
    public void bulkUpdateAgentStatus(List<String> agentUids, AgentStatus status) {
        log.info("Bulk updating status for {} agents to: {}", agentUids.size(), status);

        List<Agent> agents = agentUids.stream()
                .map(uid -> agentRepository.findByUid(uid)
                        .orElseThrow(() -> new ApiException("Agent not found with UID: " + uid, HttpStatus.NOT_FOUND)))
                .collect(Collectors.toList());

        agents.forEach(agent -> {
            agent.setStatus(status);
            if (status == AgentStatus.ACTIVE && agent.getApprovalDate() == null) {
                agent.setApprovalDate(LocalDateTime.now());
            }
            agent.setUpdatedAt(LocalDateTime.now());
        });

        agentRepository.saveAll(agents);
        log.info("Bulk status update completed successfully");
    }

    @Override
    @Transactional
    public void bulkApproveAgents(List<String> agentUids) {
        log.info("Bulk approving {} agents", agentUids.size());
        bulkUpdateAgentStatus(agentUids, AgentStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void bulkRejectAgents(List<String> agentUids) {
        log.info("Bulk rejecting {} agents", agentUids.size());
        bulkUpdateAgentStatus(agentUids, AgentStatus.REJECTED);
    }

    // Private Helper Methods

    private void validateUniqueFields(CreateAgentRequestDto createRequest) {
        // Validate partner agent number uniqueness within partner
        Partner partner = partnerRepository.findById(createRequest.getPartnerId())
            .orElseThrow(() -> new ApiException("Partner not found with ID: " + createRequest.getPartnerId(), HttpStatus.NOT_FOUND));
        
        if (agentRepository.existsByPartnerAndPartnerAgentNumber(partner, createRequest.getPartnerAgentNumber())) {
            throw new ApiException("Partner agent number already exists: " + createRequest.getPartnerAgentNumber(), HttpStatus.CONFLICT);
        }

        if (StringUtils.hasText(createRequest.getBusinessEmail()) &&
            agentRepository.existsByBusinessEmail(createRequest.getBusinessEmail())) {
            throw new ApiException("Business email already exists: " + createRequest.getBusinessEmail(), HttpStatus.CONFLICT);
        }

        if (StringUtils.hasText(createRequest.getPhoneNumber()) &&
            agentRepository.existsByPhoneNumber(createRequest.getPhoneNumber())) {
            throw new ApiException("Phone number already exists: " + createRequest.getPhoneNumber(), HttpStatus.CONFLICT);
        }

        if (StringUtils.hasText(createRequest.getMsisdn()) &&
            agentRepository.existsByPartnerAndMsisdn(partner, createRequest.getMsisdn())) {
            throw new ApiException("MSISDN already exists for this partner: " + createRequest.getMsisdn(), HttpStatus.CONFLICT);
        }

        if (StringUtils.hasText(createRequest.getTaxId()) &&
            agentRepository.existsByTaxId(createRequest.getTaxId())) {
            throw new ApiException("Tax ID already exists: " + createRequest.getTaxId(), HttpStatus.CONFLICT);
        }

        if (StringUtils.hasText(createRequest.getLicenseNumber()) &&
            agentRepository.existsByLicenseNumber(createRequest.getLicenseNumber())) {
            throw new ApiException("License number already exists: " + createRequest.getLicenseNumber(), HttpStatus.CONFLICT);
        }
    }

    private boolean validateSuperAgentAssignmentInternal(AgentType subAgentType, Agent superAgent) {
        // Sub-agent must be of type SUB_AGENT
        if (subAgentType != AgentType.SUB_AGENT) {
            return false;
        }

        // Super agent must be of type SUPER_AGENT
        if (superAgent.getAgentType() != AgentType.SUPER_AGENT) {
            return false;
        }

        // Both agents must belong to the same partner
        // This validation would need the sub-agent context, so we'll skip for now
        return true;
    }

    private AgentResponseDto mapToAgentResponseDto(Agent agent) {
        AgentResponseDto dto = new AgentResponseDto();
        dto.setId(agent.getId());
        dto.setUid(agent.getUid());
        dto.setCode(agent.getCode());
        dto.setPartnerAgentNumber(agent.getPartnerAgentNumber());
        dto.setBusinessName(agent.getBusinessName());
        dto.setContactPerson(agent.getContactPerson());
        dto.setPhoneNumber(agent.getPhoneNumber());
        dto.setBusinessEmail(agent.getBusinessEmail());
        dto.setBusinessAddress(agent.getBusinessAddress());
        dto.setTaxId(agent.getTaxId());
        dto.setLicenseNumber(agent.getLicenseNumber());
        dto.setAgentType(agent.getAgentType());
        dto.setStatus(agent.getStatus());
        dto.setRegistrationDate(agent.getRegistrationDate());
        dto.setApprovalDate(agent.getApprovalDate());
        dto.setLastActivityDate(agent.getLastActivityDate());
        dto.setNotes(agent.getNotes());
        dto.setCreatedAt(agent.getCreatedAt());
        dto.setUpdatedAt(agent.getUpdatedAt());

        // Partner information
        if (agent.getPartner() != null) {
            dto.setPartnerId(agent.getPartner().getId());
            dto.setPartnerUid(agent.getPartner().getUid());
            dto.setPartnerCode(agent.getPartner().getCode());
            dto.setPartnerBusinessName(agent.getPartner().getBusinessName());
        }

        // Super agent information
        if (agent.getSuperAgent() != null) {
            dto.setSuperAgentId(agent.getSuperAgent().getId());
            dto.setSuperAgentUid(agent.getSuperAgent().getUid());
            dto.setSuperAgentCode(agent.getSuperAgent().getCode());
            dto.setSuperAgentBusinessName(agent.getSuperAgent().getBusinessName());
        }

        // User information
        if (agent.getUser() != null) {
            dto.setUserId(agent.getUser().getId());
            dto.setUserUsername(agent.getUser().getUsername());
            dto.setUserEmail(agent.getUser().getEmail());
        }

        return dto;
    }

    private AgentSummaryDto mapToAgentSummaryDto(Agent agent) {
        AgentSummaryDto dto = new AgentSummaryDto();
        dto.setId(agent.getId());
        dto.setUid(agent.getUid());
        dto.setCode(agent.getCode());
        dto.setPartnerAgentNumber(agent.getPartnerAgentNumber());
        dto.setPassName(agent.getPassName());
        dto.setBusinessName(agent.getBusinessName());
        dto.setContactPerson(agent.getContactPerson());
        dto.setAgentType(agent.getAgentType());
        dto.setStatus(agent.getStatus());
        dto.setRegistrationDate(agent.getRegistrationDate());

        // Partner information
        if (agent.getPartner() != null) {
            dto.setPartnerId(agent.getPartner().getId());
            dto.setPartnerCode(agent.getPartner().getCode());
            dto.setPartnerBusinessName(agent.getPartner().getBusinessName());
        }

        // Super agent information
        if (agent.getSuperAgent() != null) {
            dto.setSuperAgentId(agent.getSuperAgent().getId());
            dto.setSuperAgentCode(agent.getSuperAgent().getCode());
            dto.setSuperAgentBusinessName(agent.getSuperAgent().getBusinessName());
        }

        return dto;
    }
    
    /**
     * Generate agent code with partner code and random number
     * 
     * @param partnerCode the partner code (e.g., "MIXX", "VODA")
     * @param randomDigits the number of random digits to append
     * @return agent code in format: {PARTNER_CODE}{RANDOM_DIGITS}
     */
    private String generateAgentCodeWithPartner(String partnerCode, int randomDigits) {
        if (partnerCode == null || partnerCode.trim().isEmpty()) {
            throw new ApiException("Partner code cannot be null or empty", HttpStatus.BAD_REQUEST);
        }
        
        if (randomDigits <= 0) {
            throw new ApiException("Number of random digits must be positive", HttpStatus.BAD_REQUEST);
        }
        
        Random random = new Random();
        int maxValue = (int) Math.pow(10, randomDigits) - 1; // e.g., for 4 digits: 9999
        int minValue = (int) Math.pow(10, randomDigits - 1); // e.g., for 4 digits: 1000
        
        int randomNumber = random.nextInt(maxValue - minValue + 1) + minValue;
        return partnerCode.toUpperCase() + randomNumber;
    }
}
