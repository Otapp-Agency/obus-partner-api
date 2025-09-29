package com.obuspartners.modules.common.service;

import com.obuspartners.modules.common.domain.event.EmailNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Email Notification Event Producer Service
 * Sends email notification events to Kafka for asynchronous processing
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Send email notification event to Kafka
     */
    public void sendEmailNotificationEvent(EmailNotificationEvent emailEvent) {
        try {
            log.info("Sending email notification event to Kafka: {} for recipient: {}", 
                    emailEvent.getEventType(), emailEvent.getRecipientEmail());
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send("obus.email.notification", emailEvent.getEventId(), emailEvent);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Email notification event sent successfully to Kafka, offset: {}", 
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send email notification event to Kafka: {}", emailEvent.getEventId(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error sending email notification event to Kafka: {}", emailEvent.getEventId(), e);
        }
    }

    /**
     * Send welcome email notification
     */
    public void sendWelcomeEmail(String recipientEmail, String recipientName, String businessName) {
        EmailNotificationEvent emailEvent = EmailNotificationEvent.createWelcomeEmail(
                recipientEmail, recipientName, businessName);
        
        sendEmailNotificationEvent(emailEvent);
    }

    /**
     * Send notification email
     */
    public void sendNotificationEmail(String recipientEmail, String recipientName, 
            String subject, String message) {
        
        EmailNotificationEvent emailEvent = EmailNotificationEvent.createNotificationEmail(
                recipientEmail, recipientName, subject, message);
        
        sendEmailNotificationEvent(emailEvent);
    }

    /**
     * Send custom email notification
     */
    public void sendCustomEmailNotification(String recipientEmail, String recipientName, 
            String subject, String body, String context) {
        
        EmailNotificationEvent emailEvent = EmailNotificationEvent.createCustomEmail(
                recipientEmail, recipientName, subject, body, context);
        
        sendEmailNotificationEvent(emailEvent);
    }

    /**
     * Send high priority email notification
     */
    public void sendHighPriorityEmailNotification(String recipientEmail, String recipientName, 
            String subject, String body, String context) {
        
        EmailNotificationEvent emailEvent = EmailNotificationEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("HIGH_PRIORITY_EMAIL")
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .subject(subject)
                .body(body)
                .context(context)
                .priority("HIGH")
                .maxRetries(5)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        sendEmailNotificationEvent(emailEvent);
    }
}
