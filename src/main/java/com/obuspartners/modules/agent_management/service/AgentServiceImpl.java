package com.obuspartners.modules.agent_management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.agent_management.domain.dto.*;
import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.domain.entity.PartnerAgentVerification;
import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;
import com.obuspartners.modules.agent_management.domain.enums.AgentVerificationStatus;
import com.obuspartners.modules.agent_management.domain.event.PartnerAgentVerificationRequestedEvent;
import com.obuspartners.modules.agent_management.repository.AgentRepository;
import com.obuspartners.modules.agent_management.repository.PartnerAgentVerificationRepository;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;
import com.obuspartners.modules.common.service.EmailService;
import com.obuspartners.modules.common.service.EventProducerService;

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
    private final PartnerAgentVerificationRepository verificationRepository;
    private final EmailService emailService;
    private final EventProducerService eventProducerService;

    // CRUD Operations

    @Override
    @Transactional
    public AgentResponseDto createAgent(CreateAgentRequestDto createRequest) {
        log.info("Creating new agent with partner agent number: {}", createRequest.getPartnerAgentNumber());

        // Validate uniqueness of critical fields
        validateUniqueFields(createRequest);

        // Validate partner exists
        Partner partner = partnerRepository.findById(createRequest.getPartnerId())
                .orElseThrow(() -> new ApiException("Partner not found with ID: " + createRequest.getPartnerId(), HttpStatus.NOT_FOUND));

        // Validate super agent if provided
        Agent superAgent = null;
        if (createRequest.getSuperAgentId() != null) {
            superAgent = agentRepository.findById(createRequest.getSuperAgentId())
                    .orElseThrow(() -> new ApiException("Super agent not found with ID: " + createRequest.getSuperAgentId(), HttpStatus.NOT_FOUND));
            
            // Validate super agent assignment
            validateSuperAgentAssignmentInternal(createRequest.getAgentType(), superAgent);
        }

        // Create agent entity
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
        agent.setAgentType(createRequest.getAgentType());
        agent.setPartner(partner);
        agent.setSuperAgent(superAgent);
        agent.setNotes(createRequest.getNotes());
        agent.setStatus(AgentStatus.PENDING_APPROVAL);
        agent.setRegistrationDate(LocalDateTime.now());
        
        // Generate system-wide unique code
        agent.setCode(generateAgentCode());
        
        // Generate login username
        agent.setLoginUsername(generateLoginUsername(partner.getCode(), createRequest.getPartnerAgentNumber()));
        
        // Generate login password
        agent.setLoginPassword(generateLoginPassword());

        Agent savedAgent = agentRepository.save(agent);
        log.info("Agent created successfully with UID: {}", savedAgent.getUid());

        // Create verification request
        PartnerAgentVerification verification = createVerificationRequest(savedAgent, partner);
        PartnerAgentVerification savedVerification = verificationRepository.save(verification);
        log.info("Verification request created with UID: {}", savedVerification.getUid());

        // Send Kafka event
        sendVerificationRequestedEvent(savedAgent, partner, savedVerification);

        // Send credentials email to agent
        // sendAgentCredentialsEmail(savedAgent);

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
    public Page<AgentSummaryDto> getAllAgents(Pageable pageable) {
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

        // Generate agent code based on partner code + sequence
        String baseCode = partner.getCode() + "-AGT-";
        long agentCount = agentRepository.countByPartner(partner);
        return baseCode + String.format("%04d", agentCount + 1);
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
        dto.setLoginUsername(agent.getLoginUsername());
        dto.setLoginPassword(agent.getLoginPassword());
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
        dto.setLoginUsername(agent.getLoginUsername());
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
     * Generate a unique agent code
     * 
     * @return unique agent code
     */
    private String generateAgentCode() {
        return "AGT-" + System.currentTimeMillis() + "-" + 
               java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Generate login username for agent
     * 
     * @param partnerCode the partner code
     * @param partnerAgentNumber the partner agent number
     * @return login username in format: PARTNERCODE-AGENTNUMBER
     */
    private String generateLoginUsername(String partnerCode, String partnerAgentNumber) {
        return partnerCode + "-" + partnerAgentNumber;
    }
    
    /**
     * Generate login password for agent
     * 
     * @return generated password
     */
    private String generateLoginPassword() {
        // Generate a random 8-character password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }

    /**
     * Create verification request for agent
     */
    private PartnerAgentVerification createVerificationRequest(Agent agent, Partner partner) {
        PartnerAgentVerification verification = new PartnerAgentVerification();
        verification.setPartner(partner);
        verification.setAgent(agent);
        // requestReferenceNumber will be auto-generated as ULID in @PrePersist
        verification.setAgentVerificationStatus(AgentVerificationStatus.PENDING);
        verification.setVerificationType("DOCUMENT_VERIFICATION");
        verification.setRequestedBy("SYSTEM"); // Could be passed as parameter
        verification.setPriority("NORMAL");
        verification.setRequestedAt(LocalDateTime.now());
        verification.setExpiresAt(LocalDateTime.now().plusDays(30)); // 30 days expiry
        return verification;
    }

    /**
     * Send verification requested event to Kafka
     */
    private void sendVerificationRequestedEvent(Agent agent, Partner partner, PartnerAgentVerification verification) {
        try {
            PartnerAgentVerificationRequestedEvent event = PartnerAgentVerificationRequestedEvent.create(
                    agent.getUid(),
                    agent.getCode(),
                    agent.getBusinessName(),
                    partner.getUid(),
                    partner.getCode(),
                    partner.getBusinessName(),
                    verification.getUid(),
                    verification.getRequestReferenceNumber(),
                    verification.getRequestedBy()
            );
            
            // Add additional fields
            event.setAgentContactPerson(agent.getContactPerson());
            event.setAgentMsisdn(agent.getMsisdn());
            event.setAgentBusinessEmail(agent.getBusinessEmail());
            event.setVerificationType(verification.getVerificationType());
            event.setPriority(verification.getPriority());
            event.setRequestedAt(verification.getRequestedAt());
            event.setExpiresAt(verification.getExpiresAt());
            
            eventProducerService.sendPartnerAgentVerificationRequestedEvent(event);
            log.info("Verification requested event sent for agent: {}", agent.getUid());
            
        } catch (Exception e) {
            log.error("Failed to send verification requested event for agent: {}", agent.getUid(), e);
            // Don't fail the transaction if event sending fails
        }
    }
    
    /**
     * Send agent credentials email
     * 
     * @param agent the agent entity
     */
    private void sendAgentCredentialsEmail(Agent agent) {
        try {
            String subject = "Welcome to OTAPP PARTNERS – Your Agent Account Credentials";
            String message = String.format(
                "Hello %s,\n\n" +
                "Your OTAPP PARTNERS agent account has been successfully created.\n\n" + 
                "Here are your login credentials:\n" +
                "- Agent Number: %s\n" +
                "- Login Password: %s\n\n" +
                "Please keep these credentials secure and do not share them with anyone.\n\n" +
                "Login Instructions:\n" +
                "1. Use your agent number (%s) and password to login\n" +
                "2. The system will automatically create your username\n" +
                "3. Contact your partner for login assistance if needed\n\n" +
                "If you did not request this account, please contact our support team.\n\n" +
                "Regards,\nOTAPP Support Team",
                agent.getContactPerson(),
                agent.getPartnerAgentNumber(),
                agent.getLoginPassword(),
                agent.getPartnerAgentNumber()
            );

            emailService.sendEmail(agent.getBusinessEmail(), subject, message);
            log.info("Agent credentials email sent successfully to: {}", agent.getBusinessEmail());
            
        } catch (Exception e) {
            log.error("Failed to send agent credentials email to {}: {}", agent.getBusinessEmail(), e.getMessage(), e);
            // Don't throw exception to avoid failing agent creation if email fails
        }
    }
}
