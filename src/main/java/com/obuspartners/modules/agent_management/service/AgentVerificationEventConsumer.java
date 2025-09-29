package com.obuspartners.modules.agent_management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.domain.entity.AgentRequest;
import com.obuspartners.modules.agent_management.domain.entity.PartnerAgentVerification;
import com.obuspartners.modules.agent_management.domain.enums.AgentRequestStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentVerificationStatus;
import com.obuspartners.modules.agent_management.domain.event.PartnerAgentVerificationRequestedEvent;
import com.obuspartners.modules.agent_management.repository.AgentRepository;
import com.obuspartners.modules.agent_management.repository.AgentRequestRepository;
import com.obuspartners.modules.agent_management.repository.PartnerAgentVerificationRepository;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.common.service.EmailNotificationEventProducer;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Service for consuming agent verification events from Kafka
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentVerificationEventConsumer {

        private final AgentVerificationService agentVerificationService;
        private final AgentRepository agentRepository;
        private final AgentRequestRepository agentRequestRepository;
        private final PartnerRepository partnerRepository;
        private final RestTemplate restTemplate;
        private final EmailNotificationEventProducer emailNotificationEventProducer;
        private final PartnerAgentVerificationRepository partnerAgentVerificationRepository;

        /**
         * Consume partner agent verification requested events
         */
        @KafkaListener(topics = "obus.partner.agent.verification.requested", groupId = "obus-partner-api-verification-group", containerFactory = "verificationKafkaListenerContainerFactory")
        public void handlePartnerAgentVerificationRequested(
                        @Payload PartnerAgentVerificationRequestedEvent event,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset,
                        Acknowledgment acknowledgment) {

                log.info("Received verification requested event: {} from topic: {}, partition: {}, offset: {}",
                                event.getEventId(), topic, partition, offset);

                try {
                        // Process the verification request
                        processVerificationRequest(event);

                        // Acknowledge the message
                        acknowledgment.acknowledge();

                        log.info("Successfully processed verification requested event: {}", event.getEventId());

                } catch (Exception e) {
                        log.error("Failed to process verification requested event: {}", event.getEventId(), e);

                        // In a real implementation, you might want to:
                        // 1. Send to a dead letter queue
                        // 2. Retry with exponential backoff
                        // 3. Send notification to admin
                        // For now, we'll acknowledge to prevent infinite retries
                        acknowledgment.acknowledge();
                }
        }

        /**
         * Process the verification request
         */
        private void processVerificationRequest(PartnerAgentVerificationRequestedEvent event) {
                log.info("Processing verification request for agent: {} with reference: {}",
                                event.getAgentUid(), event.getRequestReferenceNumber());

                // Here you can implement various verification workflows:

                AgentRequest agentRequest = agentRequestRepository.findByUid(event.getAgentUid())
                                .orElseThrow(() -> new ApiException("Agent request not found", HttpStatus.NOT_FOUND));

                Partner partner = partnerRepository.findByUid(event.getPartnerUid())
                                .orElseThrow(() -> new ApiException("Partner not found", HttpStatus.NOT_FOUND));

                String partnerCode = partner.getCode();
                switch (partnerCode) {
                        case "MIXX":
                                performMixxVerification(agentRequest, partner, event);
                                break;

                        default:
                                throw new ApiException("Invalid partner code", HttpStatus.BAD_REQUEST);
                }

                // 1. Send notification to verification team
                sendVerificationNotification(event);

                // 2. Create verification tasks in external systems
                createVerificationTasks(event);

                // 3. Update verification status if needed
                updateVerificationStatus(event);

                // 4. Send email/SMS notifications
                sendNotifications(event);

                // 5. Log for audit purposes
                logVerificationRequest(event);

                log.info("Completed processing verification request for agent: {}", event.getAgentUid());
        }

        /**
         * Send notification to verification team
         */
        private void sendVerificationNotification(PartnerAgentVerificationRequestedEvent event) {
                log.info("Sending verification notification for agent: {} to verification team", event.getAgentUid());

                // Implementation could include:
                // - Slack notifications
                // - Email to verification team
                // - Dashboard updates
                // - Mobile push notifications

                // Example implementation:
                String notificationMessage = String.format(
                                "New agent verification request:\n" +
                                                "Agent: %s (%s)\n" +
                                                "Partner: %s (%s)\n" +
                                                "Reference: %s\n" +
                                                "Priority: %s\n" +
                                                "Requested At: %s",
                                event.getAgentBusinessName(),
                                event.getAgentCode(),
                                event.getPartnerBusinessName(),
                                event.getPartnerCode(),
                                event.getRequestReferenceNumber(),
                                event.getPriority(),
                                event.getRequestedAt());

                log.info("Verification notification: {}", notificationMessage);
        }

        /**
         * Create verification tasks in external systems
         */
        private void createVerificationTasks(PartnerAgentVerificationRequestedEvent event) {
                log.info("Creating verification tasks for agent: {}", event.getAgentUid());

                // Implementation could include:
                // - Creating tasks in project management tools (Jira, Asana)
                // - Adding to verification queue
                // - Creating calendar events for verification team
                // - Integrating with document management systems

                // Example implementation:
                log.info("Created verification task with ID: TASK-{} for agent: {}",
                                event.getRequestReferenceNumber(), event.getAgentUid());
        }

        /**
         * Update verification status if needed
         */
        private void updateVerificationStatus(PartnerAgentVerificationRequestedEvent event) {
                log.info("Updating verification status for agent: {}", event.getAgentUid());

                // Implementation could include:
                // - Updating status in external systems
                // - Syncing with partner systems
                // - Updating dashboard metrics

                // Example implementation:
                log.info("Updated verification status to 'PROCESSING' for agent: {}", event.getAgentUid());
        }

        /**
         * Send notifications to relevant parties
         */
        private void sendNotifications(PartnerAgentVerificationRequestedEvent event) {
                log.info("Sending notifications for agent: {}", event.getAgentUid());

                // Implementation could include:
                // - Email to agent about verification status
                // - SMS to agent contact person
                // - Email to partner about verification request
                // - Push notifications to mobile apps

                // Example implementation:
                if (event.getAgentBusinessEmail() != null) {
                        log.info("Sending email notification to agent: {} at {}",
                                        event.getAgentUid(), event.getAgentBusinessEmail());
                }

                if (event.getAgentMsisdn() != null) {
                        log.info("Sending SMS notification to agent: {} at {}",
                                        event.getAgentUid(), event.getAgentMsisdn());
                }
        }

        /**
         * Log verification request for audit purposes
         */
        private void logVerificationRequest(PartnerAgentVerificationRequestedEvent event) {
                log.info("Audit log - Verification request received: " +
                                "EventId={}, AgentUid={}, PartnerUid={}, Reference={}, Priority={}, RequestedAt={}",
                                event.getEventId(),
                                event.getAgentUid(),
                                event.getPartnerUid(),
                                event.getRequestReferenceNumber(),
                                event.getPriority(),
                                event.getRequestedAt());

                // Implementation could include:
                // - Writing to audit database
                // - Sending to logging service (ELK, Splunk)
                // - Creating audit trail entries
                // - Compliance reporting
        }

        /**
         * Perform verification with MIXX (Tigo) API
         */
        private void performMixxVerification(AgentRequest agentRequest, Partner partner,
                        PartnerAgentVerificationRequestedEvent event) {
                log.info("Performing MIXX verification for agent request: {} with MSISDN: {}", agentRequest.getUid(),
                                agentRequest.getMsisdn());

                try {
                        // Prepare request for MIXX API
                        MixxAccountInfoRequest request = new MixxAccountInfoRequest();
                        request.setAgentMSISDN(agentRequest.getMsisdn());
                        request.setAgentCODE(agentRequest.getPartnerAgentNumber());
                        request.setReferenceID(event.getRequestReferenceNumber());

                        // Set up headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);

                        // Create HTTP entity
                        HttpEntity<MixxAccountInfoRequest> httpEntity = new HttpEntity<>(request, headers);

                        // Call MIXX API
                        String mixxApiUrl = "https://accessgwtest.tigo.co.tz:8443/accountInfo";
                        ResponseEntity<MixxAccountInfoResponse> response = restTemplate.postForEntity(
                                        mixxApiUrl,
                                        httpEntity,
                                        MixxAccountInfoResponse.class);

                        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                                MixxAccountInfoResponse mixxResponse = response.getBody();

                                if (mixxResponse.getResult() != null
                                                && (mixxResponse.getResult() == 0 || mixxResponse.getResult() == 1)) {
                                        // Verification successful
                                        log.info("MIXX verification successful for agent request: {} - Agent Name: {}",
                                                        agentRequest.getUid(), mixxResponse.getAgentName());

                                        // Update agent request verification status
                                        updateAgentRequestVerificationStatus(agentRequest, partner,
                                                        event.getRequestReferenceNumber(),
                                                        AgentVerificationStatus.APPROVED,
                                                        "MIXX verification successful. Agent Name: "
                                                                        + mixxResponse.getAgentName());

                                        // Create actual Agent entity
                                        createAgentFromRequest(agentRequest, partner);

                                        // Send success notification
                                        sendMixxVerificationSuccessNotification(agentRequest, partner, mixxResponse);

                                } else {
                                        // Verification failed
                                        log.warn("MIXX verification failed for agent request: {} - Result: {}, Message: {}",
                                                        agentRequest.getUid(), mixxResponse.getResult(),
                                                        mixxResponse.getMessage());

                                        // Update agent request verification status
                                        updateAgentRequestVerificationStatus(agentRequest, partner,
                                                        event.getRequestReferenceNumber(),
                                                        AgentVerificationStatus.REJECTED,
                                                        "MIXX verification failed: " + mixxResponse.getMessage());

                                        // Reject agent request
                                        agentRequest.reject("SYSTEM",
                                                        "MIXX verification failed: " + mixxResponse.getMessage());
                                        agentRequestRepository.save(agentRequest);

                                        // Send failure notification
                                        sendMixxVerificationFailureNotification(agentRequest, partner, mixxResponse);
                                }
                        } else {
                                log.error("MIXX API call failed with status: {} for agent request: {}",
                                                response.getStatusCode(), agentRequest.getUid());

                                // Update agent request verification status
                                updateAgentRequestVerificationStatus(agentRequest, partner,
                                                event.getRequestReferenceNumber(),
                                                AgentVerificationStatus.REJECTED,
                                                "MIXX API call failed with status: " + response.getStatusCode());

                                // Reject agent request
                                agentRequest.reject("SYSTEM",
                                                "MIXX API call failed with status: " + response.getStatusCode());
                                agentRequestRepository.save(agentRequest);
                        }

                } catch (Exception e) {
                        log.error("Error calling MIXX API for agent request: {}", agentRequest.getUid(), e);

                        // Update agent request verification status
                        updateAgentRequestVerificationStatus(agentRequest, partner, event.getRequestReferenceNumber(),
                                        AgentVerificationStatus.REJECTED,
                                        "MIXX API error: " + e.getMessage());

                        // Reject agent request
                        agentRequest.reject("SYSTEM", "MIXX API error: " + e.getMessage());
                        agentRequestRepository.save(agentRequest);
                        System.out.println(e.getStackTrace());
                }
        }

        /**
         * Update agent verification status
         */
        private void updateAgentVerificationStatus(Agent agent, Partner partner, String requestReferenceNumber,
                        AgentVerificationStatus status, String notes) {
                log.info("Updating verification status for agent: {} to: {}", agent.getUid(), status);

                PartnerAgentVerification verification = partnerAgentVerificationRepository
                                .findByAgentAndPartnerAndRequestReferenceNumber(agent, partner, requestReferenceNumber)
                                .orElseThrow(() -> new ApiException("Could not find verification information",
                                                HttpStatus.NOT_FOUND));
                verification.setAgentVerificationStatus(status);
                verification.setVerificationNotes(notes);
                partnerAgentVerificationRepository.save(verification);

                // Implementation could include:
                // - Updating verification status in database
                // - Updating agent status
                // - Creating verification history record
                // - Sending status update events

                log.info("Verification status updated for agent: {} to: {} with notes: {}",
                                agent.getUid(), status, notes);
        }

        /**
         * Send MIXX verification success notification
         */
        private void sendMixxVerificationSuccessNotification(AgentRequest agentRequest, Partner partner,
                        MixxAccountInfoResponse response) {
                log.info("Sending MIXX verification success notification for agent request: {}", agentRequest.getUid());

                // Send email notification to agent
                if (agentRequest.getBusinessEmail() != null && !agentRequest.getBusinessEmail().trim().isEmpty()) {
                        String agentName = agentRequest.getContactPerson() != null ? agentRequest.getContactPerson()
                                        : agentRequest.getBusinessName();
                        String emailSubject = "Agent Verification Successful - " + agentRequest.getBusinessName();
                        String emailBody = String.format(
                                        "Dear %s,\n\n" +
                                                        "Congratulations! Your agent verification has been successfully completed.\n\n"
                                                        +
                                                        "Verification Details:\n" +
                                                        "• Business Name: %s\n" +
                                                        "• Agent Name: %s\n" +
                                                        "• MSISDN: %s\n" +
                                                        "• Agent Code: %s\n" +
                                                        "• Reference ID: %s\n" +
                                                        "• Partner: %s\n" +
                                                        "• Verification Date: %s\n\n" +
                                                        "Your agent account is now active and ready to use. You can start processing transactions immediately.\n\n"
                                                        +
                                                        "If you have any questions, please contact our support team.\n\n"
                                                        +
                                                        "Best regards,\n" +
                                                        "OBUS Partners Team",
                                        agentName,
                                        agentRequest.getBusinessName(),
                                        response.getAgentName(),
                                        response.getAgentMSISDN(),
                                        response.getAgentCODE(),
                                        response.getReferenceID(),
                                        partner.getBusinessName(),
                                        java.time.LocalDateTime.now().toString());

                        emailNotificationEventProducer.sendCustomEmailNotification(
                                        agentRequest.getBusinessEmail(),
                                        agentName,
                                        emailSubject,
                                        emailBody,
                                        "AGENT_VERIFICATION_SUCCESS");

                        log.info("MIXX verification success email notification sent to agent request: {} at {}",
                                        agentRequest.getUid(), agentRequest.getBusinessEmail());
                } else {
                        log.warn("No email address available for agent request: {}", agentRequest.getUid());
                }

                // Log the notification details
                String notificationMessage = String.format(
                                "MIXX Verification Successful:\n" +
                                                "Agent Request: %s (%s)\n" +
                                                "Agent Name: %s\n" +
                                                "MSISDN: %s\n" +
                                                "Code: %s\n" +
                                                "Reference: %s\n" +
                                                "Partner: %s",
                                agentRequest.getBusinessName(),
                                agentRequest.getUid(),
                                response.getAgentName(),
                                response.getAgentMSISDN(),
                                response.getAgentCODE(),
                                response.getReferenceID(),
                                partner.getBusinessName());

                log.info("MIXX verification success notification: {}", notificationMessage);
        }

        /**
         * Send MIXX verification failure notification
         */
        private void sendMixxVerificationFailureNotification(AgentRequest agentRequest, Partner partner,
                        MixxAccountInfoResponse response) {
                log.info("Sending MIXX verification failure notification for agent request: {}", agentRequest.getUid());

                String failureReason = response.getMessage() != null ? response.getMessage() : "Unknown error";

                // Send email notification to agent
                if (agentRequest.getBusinessEmail() != null && !agentRequest.getBusinessEmail().trim().isEmpty()) {
                        String agentName = agentRequest.getContactPerson() != null ? agentRequest.getContactPerson()
                                        : agentRequest.getBusinessName();
                        String emailSubject = "Agent Verification Failed - " + agentRequest.getBusinessName();
                        String emailBody = String.format(
                                        "Dear %s,\n\n" +
                                                        "Unfortunately, your agent verification could not be completed at this time.\n\n"
                                                        +
                                                        "Verification Details:\n" +
                                                        "• Business Name: %s\n" +
                                                        "• MSISDN: %s\n" +
                                                        "• Agent Code: %s\n" +
                                                        "• Reference ID: %s\n" +
                                                        "• Partner: %s\n" +
                                                        "• Verification Date: %s\n" +
                                                        "• Failure Reason: %s\n" +
                                                        "• Result Code: %d\n\n" +
                                                        "Please review your information and contact our support team for assistance.\n\n"
                                                        +
                                                        "You may need to:\n" +
                                                        "• Verify your agent code with your partner\n" +
                                                        "• Ensure your MSISDN is correct\n" +
                                                        "• Contact your partner for verification status\n\n" +
                                                        "Best regards,\n" +
                                                        "OBUS Partners Team",
                                        agentName,
                                        agentRequest.getBusinessName(),
                                        response.getAgentMSISDN(),
                                        response.getAgentCODE(),
                                        response.getReferenceID(),
                                        partner.getBusinessName(),
                                        java.time.LocalDateTime.now().toString(),
                                        failureReason,
                                        response.getResult());

                        emailNotificationEventProducer.sendCustomEmailNotification(
                                        agentRequest.getBusinessEmail(),
                                        agentName,
                                        emailSubject,
                                        emailBody,
                                        "AGENT_VERIFICATION_FAILURE");

                        log.info("MIXX verification failure email notification sent to agent request: {} at {}",
                                        agentRequest.getUid(), agentRequest.getBusinessEmail());
                } else {
                        log.warn("No email address available for agent request: {}", agentRequest.getUid());
                }

                // Log the notification details
                String notificationMessage = String.format(
                                "MIXX Verification Failed:\n" +
                                                "Agent Request: %s (%s)\n" +
                                                "MSISDN: %s\n" +
                                                "Code: %s\n" +
                                                "Reference: %s\n" +
                                                "Result Code: %d\n" +
                                                "Message: %s\n" +
                                                "Partner: %s",
                                agentRequest.getBusinessName(),
                                agentRequest.getUid(),
                                response.getAgentMSISDN(),
                                response.getAgentCODE(),
                                response.getReferenceID(),
                                response.getResult(),
                                response.getMessage(),
                                partner.getBusinessName());

                log.info("MIXX verification failure notification: {}", notificationMessage);
        }

        /**
         * Update agent request verification status
         */
        private void updateAgentRequestVerificationStatus(AgentRequest agentRequest, Partner partner,
                        String requestReferenceNumber,
                        AgentVerificationStatus status, String notes) {
                log.info("Updating verification status for agent request: {} to: {}", agentRequest.getUid(), status);

                PartnerAgentVerification verification = partnerAgentVerificationRepository
                                .findByPartnerAndRequestReferenceNumber(partner, requestReferenceNumber)
                                .orElseThrow(() -> new ApiException("Could not find verification information",
                                                HttpStatus.NOT_FOUND));

                verification.setAgentVerificationStatus(status);
                verification.setVerificationNotes(notes);
                partnerAgentVerificationRepository.save(verification);

                log.info("Verification status updated for agent request: {} to: {} with notes: {}",
                                agentRequest.getUid(), status, notes);
        }

        /**
         * Create Agent entity from approved AgentRequest
         */
        private void createAgentFromRequest(AgentRequest agentRequest, Partner partner) {
                log.info("Creating Agent entity from approved AgentRequest: {}", agentRequest.getUid());

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
                agent.setCode(generateAgentCode());
                agent.setLoginUsername(generateLoginUsername(partner.getCode(), agentRequest.getPartnerAgentNumber()));
                agent.setLoginPassword(generateLoginPassword());

                Agent savedAgent = agentRepository.save(agent);
                log.info("Agent created successfully with UID: {}", savedAgent.getUid());

                // Update the PartnerAgentVerification record to link it to the created Agent
                PartnerAgentVerification verification = partnerAgentVerificationRepository
                                .findByPartnerAndRequestReferenceNumber(partner, agentRequest.getUid())
                                .orElseThrow(() -> new ApiException("Could not find verification information",
                                                HttpStatus.NOT_FOUND));
                verification.setAgent(savedAgent);
                partnerAgentVerificationRepository.save(verification);
                log.info("PartnerAgentVerification linked to created Agent: {}", savedAgent.getUid());

                // Update agent request status
                agentRequest.approve("SYSTEM");
                agentRequestRepository.save(agentRequest);
        }

        private String generateAgentCode() {
                return "AGT-" + System.currentTimeMillis();
        }

        private String generateLoginUsername(String partnerCode, String partnerAgentNumber) {
                return partnerCode.toLowerCase() + "_" + partnerAgentNumber.toLowerCase();
        }

        private String generateLoginPassword() {
                return "TempPass" + System.currentTimeMillis() % 10000;
        }

        // DTOs for MIXX API

        /**
         * MIXX Account Info Request DTO
         */
        @Data
        public static class MixxAccountInfoRequest {
                @JsonProperty("AgentMSISDN")
                private String agentMSISDN;

                @JsonProperty("AgentCODE")
                private String agentCODE;

                @JsonProperty("ReferenceID")
                private String referenceID;
        }

        /**
         * MIXX Account Info Response DTO
         */
        @Data
        public static class MixxAccountInfoResponse {
                @JsonProperty("Result")
                private Integer result;

                @JsonProperty("Message")
                private String message;

                @JsonProperty("AgentName")
                private String agentName;

                @JsonProperty("AgentMSISDN")
                private String agentMSISDN;

                @JsonProperty("AgentCODE")
                private String agentCODE;

                @JsonProperty("ReferenceID")
                private String referenceID;

                // For error responses
                @JsonProperty("resultCode")
                private Integer resultCode;

                @JsonProperty("resultDesc")
                private String resultDesc;
        }
}
