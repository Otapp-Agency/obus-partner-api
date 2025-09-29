package com.obuspartners.api.demo;

import com.obuspartners.modules.agent_management.domain.event.PartnerAgentVerificationRequestedEvent;
import com.obuspartners.modules.common.service.EventProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Demo controller for testing Kafka event consumption
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/demo/kafka/verification")
@RequiredArgsConstructor
public class DemoVerificationEventController {

    private final EventProducerService eventProducerService;

    /**
     * Send a test verification requested event
     */
    @PostMapping("/send-test-event")
    public ResponseEntity<Map<String, Object>> sendTestVerificationEvent(
            @RequestParam(defaultValue = "01HZ8X7K9M2N3P4Q5R6S7T8U9V") String agentUid,
            @RequestParam(defaultValue = "AGT-1703123456789-A1B2C3D4") String agentCode,
            @RequestParam(defaultValue = "Test Agent Business") String agentBusinessName,
            @RequestParam(defaultValue = "01HZ8X7K9M2N3P4Q5R6S7T8U9W") String partnerUid,
            @RequestParam(defaultValue = "PARTNER001") String partnerCode,
            @RequestParam(defaultValue = "Test Partner Business") String partnerBusinessName,
            @RequestParam(defaultValue = "01HZ8X7K9M2N3P4Q5R6S7T8U9X") String verificationUid,
            @RequestParam(defaultValue = "01HZ8X7K9M2N3P4Q5R6S7T8U9Y") String requestReferenceNumber,
            @RequestParam(defaultValue = "SYSTEM") String requestedBy) {

        log.info("Sending test verification requested event for agent: {}", agentUid);

        try {
            // Create test event
            PartnerAgentVerificationRequestedEvent event = PartnerAgentVerificationRequestedEvent.create(
                    agentUid, agentCode, agentBusinessName,
                    partnerUid, partnerCode, partnerBusinessName,
                    verificationUid, requestReferenceNumber, requestedBy
            );

            // Add additional test data
            event.setAgentContactPerson("Test Contact Person");
            event.setAgentMsisdn("255710338782");
            event.setAgentBusinessEmail("test.agent@example.com");
            event.setVerificationType("DOCUMENT_VERIFICATION");
            event.setPriority("NORMAL");
            event.setRequestedAt(LocalDateTime.now());
            event.setExpiresAt(LocalDateTime.now().plusDays(30));

            // Send event
            eventProducerService.sendPartnerAgentVerificationRequestedEvent(event);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Test verification event sent successfully",
                    "eventId", event.getEventId(),
                    "agentUid", agentUid,
                    "requestReferenceNumber", requestReferenceNumber
            ));

        } catch (Exception e) {
            log.error("Failed to send test verification event", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send test verification event: " + e.getMessage()
            ));
        }
    }

    /**
     * Send a high priority verification event
     */
    @PostMapping("/send-high-priority-event")
    public ResponseEntity<Map<String, Object>> sendHighPriorityVerificationEvent() {
        log.info("Sending high priority verification event");

        try {
            PartnerAgentVerificationRequestedEvent event = PartnerAgentVerificationRequestedEvent.create(
                    "01HZ8X7K9M2N3P4Q5R6S7T8U9Z",
                    "AGT-HIGH-001",
                    "High Priority Agent Business",
                    "01HZ8X7K9M2N3P4Q5R6S7T8U9A",
                    "PARTNER001",
                    "High Priority Partner",
                    "01HZ8X7K9M2N3P4Q5R6S7T8U9B",
                    "01HZ8X7K9M2N3P4Q5R6S7T8U9C",
                    "ADMIN"
            );

            event.setAgentContactPerson("High Priority Contact");
            event.setAgentMsisdn("255710338783");
            event.setAgentBusinessEmail("high.priority@example.com");
            event.setVerificationType("DOCUMENT_VERIFICATION");
            event.setPriority("HIGH");
            event.setRequestedAt(LocalDateTime.now());
            event.setExpiresAt(LocalDateTime.now().plusDays(7)); // Shorter expiry for high priority

            eventProducerService.sendPartnerAgentVerificationRequestedEvent(event);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "High priority verification event sent successfully",
                    "eventId", event.getEventId(),
                    "priority", "HIGH"
            ));

        } catch (Exception e) {
            log.error("Failed to send high priority verification event", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send high priority verification event: " + e.getMessage()
            ));
        }
    }

    /**
     * Send a low priority verification event
     */
    @PostMapping("/send-low-priority-event")
    public ResponseEntity<Map<String, Object>> sendLowPriorityVerificationEvent() {
        log.info("Sending low priority verification event");

        try {
            PartnerAgentVerificationRequestedEvent event = PartnerAgentVerificationRequestedEvent.create(
                    "01HZ8X7K9M2N3P4Q5R6S7T8U9D",
                    "AGT-LOW-001",
                    "Low Priority Agent Business",
                    "01HZ8X7K9M2N3P4Q5R6S7T8U9E",
                    "PARTNER001",
                    "Low Priority Partner",
                    "01HZ8X7K9M2N3P4Q5R6S7T8U9F",
                    "01HZ8X7K9M2N3P4Q5R6S7T8U9G",
                    "SYSTEM"
            );

            event.setAgentContactPerson("Low Priority Contact");
            event.setAgentMsisdn("255710338784");
            event.setAgentBusinessEmail("low.priority@example.com");
            event.setVerificationType("DOCUMENT_VERIFICATION");
            event.setPriority("LOW");
            event.setRequestedAt(LocalDateTime.now());
            event.setExpiresAt(LocalDateTime.now().plusDays(60)); // Longer expiry for low priority

            eventProducerService.sendPartnerAgentVerificationRequestedEvent(event);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Low priority verification event sent successfully",
                    "eventId", event.getEventId(),
                    "priority", "LOW"
            ));

        } catch (Exception e) {
            log.error("Failed to send low priority verification event", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send low priority verification event: " + e.getMessage()
            ));
        }
    }
}
