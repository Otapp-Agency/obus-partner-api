package com.obuspartners.modules.agent_management.service;

import com.obuspartners.modules.agent_management.domain.dto.*;
import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.domain.entity.AgentRequest;
import com.obuspartners.modules.agent_management.domain.entity.PartnerAgentVerification;
import com.obuspartners.modules.agent_management.domain.enums.AgentRequestStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;
import com.obuspartners.modules.agent_management.domain.enums.AgentVerificationStatus;
import com.obuspartners.modules.agent_management.domain.event.PartnerAgentVerificationRequestedEvent;
import com.obuspartners.modules.agent_management.repository.AgentRepository;
import com.obuspartners.modules.agent_management.repository.AgentRequestRepository;
import com.obuspartners.modules.agent_management.repository.PartnerAgentVerificationRepository;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.common.service.EventProducerService;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for AgentRequest operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentRequestServiceImpl implements AgentRequestService {

    private final AgentRequestRepository agentRequestRepository;
    private final AgentRepository agentRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerAgentVerificationRepository verificationRepository;
    private final EventProducerService eventProducerService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AgentRequestResponseDto createAgentRequest(CreateAgentRequestDto createRequest) {
        log.info("Creating new agent request with partner agent number: {}", createRequest.getPartnerAgentNumber());

        validateUniqueFields(createRequest);

        Partner partner = partnerRepository.findById(createRequest.getPartnerId())
                .orElseThrow(() -> new ApiException("Partner not found with ID: " + createRequest.getPartnerId(), HttpStatus.NOT_FOUND));

        Agent superAgent = null;
        if (createRequest.getSuperAgentId() != null) {
            superAgent = agentRepository.findById(createRequest.getSuperAgentId())
                    .orElseThrow(() -> new ApiException("Super agent not found with ID: " + createRequest.getSuperAgentId(), HttpStatus.NOT_FOUND));

            validateSuperAgentAssignmentInternal(createRequest.getAgentType(), superAgent);
        }

        AgentRequest agentRequest = new AgentRequest();
        agentRequest.setPartner(partner);
        agentRequest.setPartnerAgentNumber(createRequest.getPartnerAgentNumber());
        agentRequest.setBusinessName(createRequest.getBusinessName());
        agentRequest.setContactPerson(createRequest.getContactPerson());
        agentRequest.setPhoneNumber(createRequest.getPhoneNumber());
        agentRequest.setMsisdn(createRequest.getMsisdn());
        agentRequest.setBusinessEmail(createRequest.getBusinessEmail());
        agentRequest.setBusinessAddress(createRequest.getBusinessAddress());
        agentRequest.setTaxId(createRequest.getTaxId());
        agentRequest.setLicenseNumber(createRequest.getLicenseNumber());
        agentRequest.setAgentType(createRequest.getAgentType());
        agentRequest.setSuperAgent(superAgent);
        agentRequest.setNotes(createRequest.getNotes());
        agentRequest.setStatus(AgentRequestStatus.PENDING);

        AgentRequest savedRequest = agentRequestRepository.save(agentRequest);
        log.info("Agent request created successfully with UID: {}", savedRequest.getUid());

        // Create verification request
        PartnerAgentVerification verification = createVerificationRequest(savedRequest, partner);
        PartnerAgentVerification savedVerification = verificationRepository.save(verification);
        log.info("Verification request created with UID: {}", savedVerification.getUid());

        // Send Kafka event for verification
        sendVerificationRequestedEvent(savedRequest, partner, savedVerification);

        // Update agent request with verification reference
        savedRequest.setVerificationReferenceNumber(savedVerification.getRequestReferenceNumber());
        agentRequestRepository.save(savedRequest);

        return mapToAgentRequestResponseDto(savedRequest);
    }

    @Override
    public Optional<AgentRequestResponseDto> getAgentRequestById(Long id) {
        return agentRequestRepository.findById(id)
                .map(this::mapToAgentRequestResponseDto);
    }

    @Override
    public Optional<AgentRequestResponseDto> getAgentRequestByUid(String uid) {
        return agentRequestRepository.findByUid(uid)
                .map(this::mapToAgentRequestResponseDto);
    }

    @Override
    public Page<AgentRequestResponseDto> getAllAgentRequests(Pageable pageable) {
        return agentRequestRepository.findAll(pageable)
                .map(this::mapToAgentRequestResponseDto);
    }

    @Override
    public Page<AgentRequestResponseDto> getAgentRequestsByPartner(Long partnerId, Pageable pageable) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ApiException("Partner not found with ID: " + partnerId, HttpStatus.NOT_FOUND));

        return agentRequestRepository.findByPartner(partner, pageable)
                .map(this::mapToAgentRequestResponseDto);
    }

    @Override
    public Page<AgentRequestResponseDto> getAgentRequestsByStatus(AgentRequestStatus status, Pageable pageable) {
        return agentRequestRepository.findByStatus(status, pageable)
                .map(this::mapToAgentRequestResponseDto);
    }

    @Override
    public Page<AgentRequestResponseDto> searchAgentRequests(AgentRequestSearchRequestDto searchRequest, Pageable pageable) {
        // This would need a custom query implementation
        // For now, return all requests
        return agentRequestRepository.findAll(pageable)
                .map(this::mapToAgentRequestResponseDto);
    }

    @Override
    @Transactional
    public AgentRequestResponseDto approveAgentRequest(String uid, String processedBy) {
        log.info("Approving agent request with UID: {}", uid);

        AgentRequest agentRequest = agentRequestRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent request not found with UID: " + uid, HttpStatus.NOT_FOUND));

        if (!agentRequest.isPending()) {
            throw new ApiException("Agent request is not pending and cannot be approved", HttpStatus.BAD_REQUEST);
        }

        // Create actual Agent entity
        Agent agent = createAgentFromRequest(agentRequest);
        Agent savedAgent = agentRepository.save(agent);
        log.info("Agent created successfully with UID: {}", savedAgent.getUid());

        // Update agent request status
        agentRequest.approve(processedBy);
        AgentRequest updatedRequest = agentRequestRepository.save(agentRequest);

        return mapToAgentRequestResponseDto(updatedRequest);
    }

    @Override
    @Transactional
    public AgentRequestResponseDto rejectAgentRequest(String uid, String processedBy, String rejectionReason) {
        log.info("Rejecting agent request with UID: {}", uid);

        AgentRequest agentRequest = agentRequestRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent request not found with UID: " + uid, HttpStatus.NOT_FOUND));

        if (!agentRequest.isPending()) {
            throw new ApiException("Agent request is not pending and cannot be rejected", HttpStatus.BAD_REQUEST);
        }

        agentRequest.reject(processedBy, rejectionReason);
        AgentRequest updatedRequest = agentRequestRepository.save(agentRequest);

        return mapToAgentRequestResponseDto(updatedRequest);
    }

    @Override
    @Transactional
    public AgentRequestResponseDto cancelAgentRequest(String uid, String processedBy) {
        log.info("Cancelling agent request with UID: {}", uid);

        AgentRequest agentRequest = agentRequestRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent request not found with UID: " + uid, HttpStatus.NOT_FOUND));

        if (!agentRequest.isPending()) {
            throw new ApiException("Agent request is not pending and cannot be cancelled", HttpStatus.BAD_REQUEST);
        }

        agentRequest.cancel(processedBy);
        AgentRequest updatedRequest = agentRequestRepository.save(agentRequest);

        return mapToAgentRequestResponseDto(updatedRequest);
    }

    @Override
    public List<AgentRequestResponseDto> getExpiredAgentRequests() {
        List<AgentRequest> expiredRequests = agentRequestRepository.findExpiredRequests(LocalDateTime.now());
        return expiredRequests.stream()
                .map(this::mapToAgentRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processExpiredAgentRequests() {
        log.info("Processing expired agent requests");

        List<AgentRequest> expiredRequests = agentRequestRepository.findExpiredRequests(LocalDateTime.now());
        
        for (AgentRequest request : expiredRequests) {
            request.expire();
            agentRequestRepository.save(request);
            log.info("Agent request expired: {}", request.getUid());
        }

        log.info("Processed {} expired agent requests", expiredRequests.size());
    }

    @Override
    public AgentRequestStatsDto getAgentRequestStats() {
        long totalRequests = agentRequestRepository.count();
        long pendingRequests = agentRequestRepository.countByStatus(AgentRequestStatus.PENDING);
        long approvedRequests = agentRequestRepository.countByStatus(AgentRequestStatus.APPROVED);
        long rejectedRequests = agentRequestRepository.countByStatus(AgentRequestStatus.REJECTED);
        long cancelledRequests = agentRequestRepository.countByStatus(AgentRequestStatus.CANCELLED);
        long expiredRequests = agentRequestRepository.countByStatus(AgentRequestStatus.EXPIRED);

        Map<AgentRequestStatus, Long> statusCounts = new HashMap<>();
        statusCounts.put(AgentRequestStatus.PENDING, pendingRequests);
        statusCounts.put(AgentRequestStatus.APPROVED, approvedRequests);
        statusCounts.put(AgentRequestStatus.REJECTED, rejectedRequests);
        statusCounts.put(AgentRequestStatus.CANCELLED, cancelledRequests);
        statusCounts.put(AgentRequestStatus.EXPIRED, expiredRequests);

        double approvalRate = totalRequests > 0 ? (double) approvedRequests / totalRequests * 100 : 0;
        double rejectionRate = totalRequests > 0 ? (double) rejectedRequests / totalRequests * 100 : 0;

        return AgentRequestStatsDto.builder()
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .approvedRequests(approvedRequests)
                .rejectedRequests(rejectedRequests)
                .cancelledRequests(cancelledRequests)
                .expiredRequests(expiredRequests)
                .statusCounts(statusCounts)
                .approvalRate(approvalRate)
                .rejectionRate(rejectionRate)
                .build();
    }

    @Override
    public AgentRequestStatsDto getAgentRequestStatsByPartner(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ApiException("Partner not found with ID: " + partnerId, HttpStatus.NOT_FOUND));

        long totalRequests = agentRequestRepository.countByPartner(partner);
        long pendingRequests = agentRequestRepository.findByPartner(partner, Pageable.unpaged())
                .getContent().stream()
                .mapToLong(ar -> ar.getStatus() == AgentRequestStatus.PENDING ? 1 : 0)
                .sum();
        // Similar calculations for other statuses...

        return AgentRequestStatsDto.builder()
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .build();
    }

    // Helper methods
    private void validateUniqueFields(CreateAgentRequestDto createRequest) {
        Partner partner = partnerRepository.findById(createRequest.getPartnerId())
                .orElseThrow(() -> new ApiException("Partner not found", HttpStatus.NOT_FOUND));

        if (agentRequestRepository.existsByPartnerAndPartnerAgentNumber(partner, createRequest.getPartnerAgentNumber())) {
            throw new ApiException("Partner agent number already exists for this partner: " + createRequest.getPartnerAgentNumber(), HttpStatus.CONFLICT);
        }

        if (StringUtils.hasText(createRequest.getMsisdn()) &&
            agentRequestRepository.existsByPartnerAndMsisdn(partner, createRequest.getMsisdn())) {
            throw new ApiException("MSISDN already exists for this partner: " + createRequest.getMsisdn(), HttpStatus.CONFLICT);
        }
    }

    private void validateSuperAgentAssignmentInternal(AgentType agentType, Agent superAgent) {
        if (agentType == AgentType.INDIVIDUAL && superAgent.getAgentType() != AgentType.CORPORATE) {
            throw new ApiException("Individual agents can only be assigned to corporate super agents", HttpStatus.BAD_REQUEST);
        }
    }

    private PartnerAgentVerification createVerificationRequest(AgentRequest agentRequest, Partner partner) {
        PartnerAgentVerification verification = new PartnerAgentVerification();
        verification.setPartner(partner);
        verification.setAgent(null); // No agent yet, will be set after approval
        verification.setAgentRequest(agentRequest); // Link to the agent request
        verification.setAgentVerificationStatus(AgentVerificationStatus.PENDING);
        verification.setVerificationType("DOCUMENT_VERIFICATION");
        verification.setRequestedBy("SYSTEM");
        verification.setPriority("NORMAL");
        verification.setRequestedAt(LocalDateTime.now());
        verification.setExpiresAt(LocalDateTime.now().plusDays(30));
        return verification;
    }

    private Agent createAgentFromRequest(AgentRequest agentRequest) {
        Agent agent = new Agent();
        agent.setPartnerAgentNumber(agentRequest.getPartnerAgentNumber());
        agent.setBusinessName(agentRequest.getBusinessName());
        agent.setContactPerson(agentRequest.getContactPerson());
        agent.setPhoneNumber(agentRequest.getPhoneNumber());
        agent.setMsisdn(agentRequest.getMsisdn());
        agent.setBusinessEmail(agentRequest.getBusinessEmail());
        agent.setBusinessAddress(agentRequest.getBusinessAddress());
        agent.setTaxId(agentRequest.getTaxId());
        agent.setLicenseNumber(agentRequest.getLicenseNumber());
        agent.setAgentType(agentRequest.getAgentType());
        agent.setPartner(agentRequest.getPartner());
        agent.setSuperAgent(agentRequest.getSuperAgent());
        agent.setNotes(agentRequest.getNotes());
        agent.setStatus(AgentStatus.ACTIVE);
        agent.setRegistrationDate(LocalDateTime.now());

        // Generate agent code and credentials
        agent.setCode(generateAgentCode(agentRequest.getPartner().getCode()));
        agent.setPassName(generateLoginUsername(agentRequest.getPartner().getCode(), agentRequest.getPartnerAgentNumber()));
        agent.setPassCode(passwordEncoder.encode(generateLoginPassword(6)));

        return agent;
    }

    private String generateAgentCode(String partnerCode) {
        return generateAgentCodeWithPartner(partnerCode, 4);
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

    private String generateLoginUsername(String partnerCode, String partnerAgentNumber) {
        return partnerCode.toLowerCase() + "_" + partnerAgentNumber.toLowerCase();
    }

    private String generateLoginPassword(int digits) {
        return generatePassCode(digits);
    }

    /**
     * Generate a flexible passcode with specified number of digits
     * 
     * @param digits the number of digits in the passcode
     * @return a passcode with the specified number of digits (doesn't start with 0)
     */
    private String generatePassCode(int digits) {
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

    private AgentRequestResponseDto mapToAgentRequestResponseDto(AgentRequest agentRequest) {
        return AgentRequestResponseDto.builder()
                .id(agentRequest.getId())
                .uid(agentRequest.getUid())
                .partnerId(agentRequest.getPartner().getId())
                .partnerCode(agentRequest.getPartner().getCode())
                .partnerBusinessName(agentRequest.getPartner().getBusinessName())
                .partnerAgentNumber(agentRequest.getPartnerAgentNumber())
                .businessName(agentRequest.getBusinessName())
                .contactPerson(agentRequest.getContactPerson())
                .phoneNumber(agentRequest.getPhoneNumber())
                .msisdn(agentRequest.getMsisdn())
                .businessEmail(agentRequest.getBusinessEmail())
                .businessAddress(agentRequest.getBusinessAddress())
                .taxId(agentRequest.getTaxId())
                .licenseNumber(agentRequest.getLicenseNumber())
                .agentType(agentRequest.getAgentType())
                .superAgentId(agentRequest.getSuperAgent() != null ? agentRequest.getSuperAgent().getId() : null)
                .superAgentCode(agentRequest.getSuperAgent() != null ? agentRequest.getSuperAgent().getCode() : null)
                .superAgentBusinessName(agentRequest.getSuperAgent() != null ? agentRequest.getSuperAgent().getBusinessName() : null)
                .notes(agentRequest.getNotes())
                .status(agentRequest.getStatus())
                .requestedAt(agentRequest.getRequestedAt())
                .processedAt(agentRequest.getProcessedAt())
                .processedBy(agentRequest.getProcessedBy())
                .rejectionReason(agentRequest.getRejectionReason())
                .verificationReferenceNumber(agentRequest.getVerificationReferenceNumber())
                .expiresAt(agentRequest.getExpiresAt())
                .createdAt(agentRequest.getCreatedAt())
                .updatedAt(agentRequest.getUpdatedAt())
                .build();
    }

    /**
     * Send verification requested event to Kafka
     */
    private void sendVerificationRequestedEvent(AgentRequest agentRequest, Partner partner, PartnerAgentVerification verification) {
        try {
            PartnerAgentVerificationRequestedEvent event = PartnerAgentVerificationRequestedEvent.create(
                    agentRequest.getUid(),
                    null, // Agent code not yet generated
                    agentRequest.getBusinessName(),
                    partner.getUid(),
                    partner.getCode(),
                    partner.getBusinessName(),
                    verification.getUid(),
                    verification.getRequestReferenceNumber(),
                    verification.getRequestedBy()
            );
            
            // Add additional fields
            event.setAgentContactPerson(agentRequest.getContactPerson());
            event.setAgentMsisdn(agentRequest.getMsisdn());
            event.setAgentBusinessEmail(agentRequest.getBusinessEmail());
            event.setVerificationType(verification.getVerificationType());
            event.setPriority(verification.getPriority());
            event.setRequestedAt(verification.getRequestedAt());
            event.setExpiresAt(verification.getExpiresAt());
            
            eventProducerService.sendPartnerAgentVerificationRequestedEvent(event);
            log.info("Verification requested event sent for agent request: {}", agentRequest.getUid());
            
        } catch (Exception e) {
            log.error("Failed to send verification requested event for agent request: {}", agentRequest.getUid(), e);
            // Don't fail the transaction if event sending fails
        }
    }
}
