package com.obuspartners.modules.common.service;

import com.obuspartners.modules.common.domain.event.DemoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Service for consuming events from Kafka topics
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class EventConsumerService {

    /**
     * Consume demo events from the demo topic
     */
    @KafkaListener(topics = "obus.demo", groupId = "obus-demo-consumer")
    public void consumeDemoEvent(
            @Payload DemoEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received demo event from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.info("Event details: {}", event);
            
            // Process the event
            processDemoEvent(event);
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing demo event: {}", event, e);
            // Don't acknowledge on error - message will be retried
        }
    }

    /**
     * Consume partner registered events
     */
    @KafkaListener(topics = "obus.partner.registered", groupId = "obus-partner-consumer")
    public void consumePartnerRegisteredEvent(
            @Payload DemoEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received partner registered event from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.info("Event details: {}", event);
            
            // Process the partner registration event
            processPartnerRegisteredEvent(event);
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing partner registered event: {}", event, e);
            // Don't acknowledge on error - message will be retried
        }
    }

    /**
     * Process demo event
     */
    private void processDemoEvent(DemoEvent event) {
        log.info("Processing demo event: {}", event.getMessage());
        
        // Add your business logic here
        // For example: send notification, update database, etc.
        
        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Demo event processed successfully");
    }

    /**
     * Process partner registered event
     */
    private void processPartnerRegisteredEvent(DemoEvent event) {
        log.info("Processing partner registered event: {}", event.getMessage());
        
        // Add your business logic here
        // For example: send welcome email, create partner dashboard, etc.
        
        // Simulate some processing time
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Partner registered event processed successfully");
    }
}
