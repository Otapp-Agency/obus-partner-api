package com.obuspartners.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Topics Configuration for OBUS Partners API
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Configuration
public class KafkaTopicsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Kafka Admin Configuration
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        KafkaAdmin admin = new KafkaAdmin(configs);
        admin.setAutoCreate(true); // Enable auto-creation of topics
        return admin;
    }

    /**
     * Demo Topic for Testing
     */
    @Bean
    public NewTopic demoTopic() {
        return TopicBuilder.name("obus.demo")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 days
                .build();
    }

    /**
     * Partner Management Topics
     */
    @Bean
    public NewTopic partnerRegisteredTopic() {
        return TopicBuilder.name("obus.partner.registered")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 days
                .build();
    }

    @Bean
    public NewTopic partnerAgentVerificationRequestedTopic() {
        return TopicBuilder.name("obus.partner.agent.verification.requested")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "2592000000") // 30 days
                .build();
    }

    @Bean
    public NewTopic emailNotificationTopic() {
        return TopicBuilder.name("obus.email.notification")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 days
                .build();
    }

    // @Bean
    // public NewTopic partnerUpdatedTopic() {
    //     return TopicBuilder.name("obus.partner.updated")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "604800000") // 7 days
    //             .build();
    // }

    // @Bean
    // public NewTopic partnerApiKeyGeneratedTopic() {
    //     return TopicBuilder.name("obus.partner.apikey.generated")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "2592000000") // 30 days
    //             .build();
    // }

    // @Bean
    // public NewTopic partnerApiKeyRevokedTopic() {
    //     return TopicBuilder.name("obus.partner.apikey.revoked")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "2592000000") // 30 days
    //             .build();
    // }

    // /**
    //  * Agent Management Topics
    //  */
    // @Bean
    // public NewTopic agentRegisteredTopic() {
    //     return TopicBuilder.name("obus.agent.registered")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "604800000") // 7 days
    //             .build();
    // }

    // @Bean
    // public NewTopic agentAuthenticatedTopic() {
    //     return TopicBuilder.name("obus.agent.authenticated")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "2592000000") // 30 days
    //             .build();
    // }

    // @Bean
    // public NewTopic agentVerificationCompletedTopic() {
    //     return TopicBuilder.name("obus.agent.verification.completed")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "604800000") // 7 days
    //             .build();
    // }

    // /**
    //  * Booking Management Topics
    //  */
    // @Bean
    // public NewTopic bookingCreatedTopic() {
    //     return TopicBuilder.name("obus.booking.created")
    //             .partitions(5)
    //             .replicas(1)
    //             .config("retention.ms", "2592000000") // 30 days
    //             .build();
    // }

    // @Bean
    // public NewTopic bookingUpdatedTopic() {
    //     return TopicBuilder.name("obus.booking.updated")
    //             .partitions(5)
    //             .replicas(1)
    //             .config("retention.ms", "2592000000") // 30 days
    //             .build();
    // }

    // @Bean
    // public NewTopic bookingCancelledTopic() {
    //     return TopicBuilder.name("obus.booking.cancelled")
    //             .partitions(5)
    //             .replicas(1)
    //             .config("retention.ms", "2592000000") // 30 days
    //             .build();
    // }

    // /**
    //  * Payment Management Topics
    //  */
    // @Bean
    // public NewTopic paymentInitiatedTopic() {
    //     return TopicBuilder.name("obus.payment.initiated")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "2592000000") // 30 days
    //             .build();
    // }

    // @Bean
    // public NewTopic paymentCompletedTopic() {
    //     return TopicBuilder.name("obus.payment.completed")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "2592000000") // 30 days
    //             .build();
    // }

    // @Bean
    // public NewTopic paymentFailedTopic() {
    //     return TopicBuilder.name("obus.payment.failed")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "2592000000") // 30 days
    //             .build();
    // }

    // /**
    //  * Audit and Security Topics
    //  */
    // @Bean
    // public NewTopic userActionTopic() {
    //     return TopicBuilder.name("obus.audit.user.action")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "7776000000") // 90 days
    //             .build();
    // }

    // @Bean
    // public NewTopic securityEventTopic() {
    //     return TopicBuilder.name("obus.security.event")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "7776000000") // 90 days
    //             .build();
    // }

    // /**
    //  * Notification Topics
    //  */
    // @Bean
    // public NewTopic notificationTopic() {
    //     return TopicBuilder.name("obus.notification")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "86400000") // 1 day
    //             .build();
    // }

    // /**
    //  * Dead Letter Queue Topic
    //  */
    // @Bean
    // public NewTopic deadLetterQueueTopic() {
    //     return TopicBuilder.name("obus.dlq")
    //             .partitions(3)
    //             .replicas(1)
    //             .config("retention.ms", "604800000") // 7 days
    //             .build();
    // }
}
