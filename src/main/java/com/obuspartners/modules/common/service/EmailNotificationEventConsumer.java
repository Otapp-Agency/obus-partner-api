package com.obuspartners.modules.common.service;

import com.obuspartners.modules.common.domain.event.EmailNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

/**
 * Email Notification Event Consumer Service
 * Consumes email notification events from Kafka and sends emails using EmailService
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationEventConsumer {

    private final EmailService emailService;

    /**
     * Consume email notification events from Kafka
     */
    @KafkaListener(
        topics = "obus.email.notification",
        groupId = "obus-email-notification-group",
        containerFactory = "verificationKafkaListenerContainerFactory"
    )
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void handleEmailNotificationEvent(
            @Payload EmailNotificationEvent emailEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received email notification event: {} from topic: {}, partition: {}, offset: {}", 
                emailEvent.getEventId(), topic, partition, offset);

        try {
            // Process the email notification
            processEmailNotification(emailEvent);
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
            log.info("Successfully processed email notification event: {}", emailEvent.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process email notification event: {}", emailEvent.getEventId(), e);
            throw e; // This will trigger retry logic
        }
    }

    /**
     * Process the email notification
     */
    private void processEmailNotification(EmailNotificationEvent emailEvent) {
        log.info("Processing email notification for recipient: {} with event type: {}", 
                emailEvent.getRecipientEmail(), emailEvent.getEventType());

        try {
            // Validate email event
            validateEmailEvent(emailEvent);
            
            // Send email using EmailService
            sendEmail(emailEvent);
            
            // Log success
            log.info("Email sent successfully to: {} for event: {}", 
                    emailEvent.getRecipientEmail(), emailEvent.getEventType());
            
        } catch (Exception e) {
            log.error("Error processing email notification for recipient: {}", 
                    emailEvent.getRecipientEmail(), e);
            throw e;
        }
    }

    /**
     * Validate email event data
     */
    private void validateEmailEvent(EmailNotificationEvent emailEvent) {
        log.debug("Validating email event: {}", emailEvent.getEventId());
        
        if (emailEvent.getRecipientEmail() == null || emailEvent.getRecipientEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is required");
        }
        
        if (emailEvent.getSubject() == null || emailEvent.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Email subject is required");
        }
        
        if (emailEvent.getBody() == null || emailEvent.getBody().trim().isEmpty()) {
            throw new IllegalArgumentException("Email body is required");
        }
        
        // Basic email format validation
        if (!emailEvent.getRecipientEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format: " + emailEvent.getRecipientEmail());
        }
        
        log.debug("Email event validation passed: {}", emailEvent.getEventId());
    }

    /**
     * Send email using EmailService
     */
    private void sendEmail(EmailNotificationEvent emailEvent) {
        log.info("Sending email to: {} with subject: {}", 
                emailEvent.getRecipientEmail(), emailEvent.getSubject());
        
        try {
            emailService.sendEmail(
                emailEvent.getRecipientEmail(),
                emailEvent.getSubject(),
                emailEvent.getBody()
            );
            
            log.info("Email sent successfully to: {} for event: {}", 
                    emailEvent.getRecipientEmail(), emailEvent.getEventType());
            
        } catch (Exception e) {
            log.error("Failed to send email to: {} for event: {}", 
                    emailEvent.getRecipientEmail(), emailEvent.getEventType(), e);
            throw e;
        }
    }
}
