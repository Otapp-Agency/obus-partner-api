package com.obuspartners.modules.common.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Email Notification Event for Kafka-based email processing
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationEvent {
    
    private String eventId;
    private String eventType;
    
    // Email details
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String body;
    
    // Sender details
    private String senderEmail;
    private String senderName;
    
    // Priority and retry
    private String priority; // HIGH, NORMAL, LOW
    private Integer retryCount;
    private Integer maxRetries;
    
    // Context and metadata
    private String context; // AGENT_VERIFICATION, PARTNER_REGISTRATION, etc.
    private Map<String, Object> metadata;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    // Factory methods for common email types
    public static EmailNotificationEvent createWelcomeEmail(String recipientEmail, String recipientName, 
            String businessName) {
        return EmailNotificationEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("WELCOME_EMAIL")
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .subject("Welcome to OBUS Partners - " + businessName)
                .body(String.format(
                    "Dear %s,\n\n" +
                    "Welcome to OBUS Partners!\n\n" +
                    "Your account has been successfully created for %s.\n\n" +
                    "You can now access your dashboard and start using our services.\n\n" +
                    "If you have any questions, please don't hesitate to contact our support team.\n\n" +
                    "Best regards,\n" +
                    "OBUS Partners Team",
                    recipientName, businessName
                ))
                .context("WELCOME")
                .priority("NORMAL")
                .maxRetries(3)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static EmailNotificationEvent createNotificationEmail(String recipientEmail, String recipientName, 
            String subject, String message) {
        return EmailNotificationEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("NOTIFICATION_EMAIL")
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .subject(subject)
                .body(String.format(
                    "Dear %s,\n\n" +
                    "%s\n\n" +
                    "Best regards,\n" +
                    "OBUS Partners Team",
                    recipientName, message
                ))
                .context("NOTIFICATION")
                .priority("NORMAL")
                .maxRetries(3)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static EmailNotificationEvent createCustomEmail(String recipientEmail, String recipientName, 
            String subject, String body, String context) {
        return EmailNotificationEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("CUSTOM_EMAIL")
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .subject(subject)
                .body(body)
                .context(context)
                .priority("NORMAL")
                .maxRetries(3)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
