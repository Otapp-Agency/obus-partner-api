package com.obuspartners.modules.common.domain.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Demo Event for Kafka testing
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoEvent {
    
    private String eventId;
    private String eventType;
    private String message;
    private String source;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Map<String, Object> metadata;
    
    public static DemoEvent create(String message, String source) {
        return DemoEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("DEMO_EVENT")
                .message(message)
                .source(source)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
