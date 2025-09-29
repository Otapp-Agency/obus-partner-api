package com.obuspartners.api.demo;

import com.obuspartners.modules.common.service.EmailNotificationEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Demo controller for testing email notification events
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/demo/email")
@RequiredArgsConstructor
public class DemoEmailNotificationController {

    private final EmailNotificationEventProducer emailNotificationEventProducer;

    /**
     * Send test welcome email
     */
    @PostMapping("/send-welcome")
    public ResponseEntity<Map<String, Object>> sendWelcomeEmail(
            @RequestParam(defaultValue = "test@example.com") String recipientEmail,
            @RequestParam(defaultValue = "John Doe") String recipientName,
            @RequestParam(defaultValue = "Test Business") String businessName) {

        log.info("Sending test welcome email to: {}", recipientEmail);

        try {
            emailNotificationEventProducer.sendWelcomeEmail(recipientEmail, recipientName, businessName);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Welcome email sent successfully",
                    "recipient", recipientEmail,
                    "type", "WELCOME_EMAIL"
            ));

        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send email: " + e.getMessage()
            ));
        }
    }

    /**
     * Send test notification email
     */
    @PostMapping("/send-notification")
    public ResponseEntity<Map<String, Object>> sendNotificationEmail(
            @RequestParam(defaultValue = "test@example.com") String recipientEmail,
            @RequestParam(defaultValue = "John Doe") String recipientName,
            @RequestParam(defaultValue = "Test Notification") String subject,
            @RequestParam(defaultValue = "This is a test notification message.") String message) {

        log.info("Sending test notification email to: {}", recipientEmail);

        try {
            emailNotificationEventProducer.sendNotificationEmail(recipientEmail, recipientName, subject, message);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification email sent successfully",
                    "recipient", recipientEmail,
                    "type", "NOTIFICATION_EMAIL"
            ));

        } catch (Exception e) {
            log.error("Failed to send notification email", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send email: " + e.getMessage()
            ));
        }
    }

    /**
     * Send test custom email
     */
    @PostMapping("/send-custom-email")
    public ResponseEntity<Map<String, Object>> sendCustomEmail(
            @RequestParam(defaultValue = "test@example.com") String recipientEmail,
            @RequestParam(defaultValue = "Test User") String recipientName,
            @RequestParam(defaultValue = "Test Subject") String subject,
            @RequestParam(defaultValue = "This is a test email body.") String body) {

        log.info("Sending test custom email to: {}", recipientEmail);

        try {
            emailNotificationEventProducer.sendCustomEmailNotification(
                    recipientEmail, recipientName, subject, body, "TEST");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Custom email sent successfully",
                    "recipient", recipientEmail,
                    "type", "CUSTOM_EMAIL"
            ));

        } catch (Exception e) {
            log.error("Failed to send custom email", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send email: " + e.getMessage()
            ));
        }
    }

    /**
     * Send test high priority email
     */
    @PostMapping("/send-high-priority-email")
    public ResponseEntity<Map<String, Object>> sendHighPriorityEmail(
            @RequestParam(defaultValue = "admin@example.com") String recipientEmail,
            @RequestParam(defaultValue = "Admin User") String recipientName,
            @RequestParam(defaultValue = "URGENT: System Alert") String subject,
            @RequestParam(defaultValue = "This is an urgent system alert that requires immediate attention.") String body) {

        log.info("Sending test high priority email to: {}", recipientEmail);

        try {
            emailNotificationEventProducer.sendHighPriorityEmailNotification(
                    recipientEmail, recipientName, subject, body, "SYSTEM_ALERT");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "High priority email sent successfully",
                    "recipient", recipientEmail,
                    "type", "HIGH_PRIORITY_EMAIL"
            ));

        } catch (Exception e) {
            log.error("Failed to send high priority email", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to send email: " + e.getMessage()
            ));
        }
    }
}
