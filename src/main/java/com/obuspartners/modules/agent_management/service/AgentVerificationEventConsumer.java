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
import com.obuspartners.modules.agent_management.domain.event.PartnerAgentVerificationRequestedEvent;
import com.obuspartners.modules.agent_management.repository.AgentRepository;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
    private final PartnerRepository partnerRepository;
    private final RestTemplate restTemplate;

    /**
     * Consume partner agent verification requested events
     */
    @KafkaListener(
        topics = "obus.partner.agent.verification.requested",
        groupId = "obus-partner-api-verification-group",
        containerFactory = "verificationKafkaListenerContainerFactory"
    )
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

        Agent agent = agentRepository.findByUid(event.getAgentUid())
        .orElseThrow(() -> new ApiException("Agent not found", HttpStatus.NOT_FOUND));

        Partner partner = partnerRepository.findByUid(event.getPartnerUid())
        .orElseThrow(() -> new ApiException("Partner not found", HttpStatus.NOT_FOUND));

        String partnerCode = partner.getCode();
        switch (partnerCode) {
            case "MIXX":
                performMixxVerification(agent, partner, event);
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
            event.getRequestedAt()
        );
        
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
    private void performMixxVerification(Agent agent, Partner partner, PartnerAgentVerificationRequestedEvent event) {
        log.info("Performing MIXX verification for agent: {} with MSISDN: {}", agent.getUid(), agent.getMsisdn());

        try {
            // Prepare request for MIXX API
            MixxAccountInfoRequest request = new MixxAccountInfoRequest();
            request.setAgentMSISDN(agent.getMsisdn());
            request.setAgentCODE(agent.getPartnerAgentNumber());
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
                MixxAccountInfoResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                MixxAccountInfoResponse mixxResponse = response.getBody();
                
                if (mixxResponse != null && mixxResponse.getResult() != null && mixxResponse.getResult() == 0) {
                    // Verification successful
                    log.info("MIXX verification successful for agent: {} - Agent Name: {}", 
                            agent.getUid(), mixxResponse.getAgentName());
                    
                    // Update agent verification status
                    updateAgentVerificationStatus(agent, "APPROVED", 
                        "MIXX verification successful. Agent Name: " + mixxResponse.getAgentName());
                    
                    // Send success notification
                    sendMixxVerificationSuccessNotification(agent, partner, mixxResponse);
                    
                } else {
                    // Verification failed
                    log.warn("MIXX verification failed for agent: {} - Result: {}, Message: {}", 
                            agent.getUid(), mixxResponse.getResult(), mixxResponse.getMessage());
                    
                    // Update agent verification status
                    updateAgentVerificationStatus(agent, "REJECTED", 
                        "MIXX verification failed: " + mixxResponse.getMessage());
                    
                    // Send failure notification
                    sendMixxVerificationFailureNotification(agent, partner, mixxResponse);
                }
            } else {
                log.error("MIXX API call failed with status: {} for agent: {}", 
                        response.getStatusCode(), agent.getUid());
                
                // Update agent verification status
                updateAgentVerificationStatus(agent, "REJECTED", 
                    "MIXX API call failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error calling MIXX API for agent: {}", agent.getUid(), e);
            
            // Update agent verification status
            updateAgentVerificationStatus(agent, "REJECTED", 
                "MIXX API error: " + e.getMessage());
        }
    }

    /**
     * Update agent verification status
     */
    private void updateAgentVerificationStatus(Agent agent, String status, String notes) {
        log.info("Updating verification status for agent: {} to: {}", agent.getUid(), status);
        
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
    private void sendMixxVerificationSuccessNotification(Agent agent, Partner partner, MixxAccountInfoResponse response) {
        log.info("Sending MIXX verification success notification for agent: {}", agent.getUid());
        
        String notificationMessage = String.format(
            "MIXX Verification Successful:\n" +
            "Agent: %s (%s)\n" +
            "Agent Name: %s\n" +
            "MSISDN: %s\n" +
            "Code: %s\n" +
            "Reference: %s\n" +
            "Partner: %s",
            agent.getBusinessName(),
            agent.getUid(),
            response.getAgentName(),
            response.getAgentMSISDN(),
            response.getAgentCODE(),
            response.getReferenceID(),
            partner.getBusinessName()
        );
        
        log.info("MIXX verification success notification: {}", notificationMessage);
    }

    /**
     * Send MIXX verification failure notification
     */
    private void sendMixxVerificationFailureNotification(Agent agent, Partner partner, MixxAccountInfoResponse response) {
        log.info("Sending MIXX verification failure notification for agent: {}", agent.getUid());
        
        String notificationMessage = String.format(
            "MIXX Verification Failed:\n" +
            "Agent: %s (%s)\n" +
            "MSISDN: %s\n" +
            "Code: %s\n" +
            "Reference: %s\n" +
            "Result Code: %d\n" +
            "Message: %s\n" +
            "Partner: %s",
            agent.getBusinessName(),
            agent.getUid(),
            response.getAgentMSISDN(),
            response.getAgentCODE(),
            response.getReferenceID(),
            response.getResult(),
            response.getMessage(),
            partner.getBusinessName()
        );
        
        log.info("MIXX verification failure notification: {}", notificationMessage);
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
