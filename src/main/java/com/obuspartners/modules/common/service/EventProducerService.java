package com.obuspartners.modules.common.service;

import com.obuspartners.modules.common.domain.event.DemoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for producing events to Kafka topics
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Send a demo event to the demo topic
     */
    public void sendDemoEvent(String message, String source) {
        DemoEvent event = DemoEvent.create(message, source);
        sendEvent("obus.demo", event.getEventId(), event);
    }

    /**
     * Send an event to a specific topic
     */
    public void sendEvent(String topic, String key, Object event) {
        try {
            log.info("Sending event to topic: {}, key: {}, event: {}", topic, key, event);
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(topic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Event sent successfully to topic: {}, offset: {}", 
                            topic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send event to topic: {}", topic, ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error sending event to topic: {}", topic, e);
        }
    }

    /**
     * Send a partner registered event
     */
    public void sendPartnerRegisteredEvent(String partnerId, String partnerName) {
        DemoEvent event = DemoEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("PARTNER_REGISTERED")
                .message("Partner registered: " + partnerName)
                .source("PARTNER_SERVICE")
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        sendEvent("obus.partner.registered", partnerId, event);
    }
}
