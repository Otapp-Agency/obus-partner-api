package com.obuspartners.api.demo;

import com.obuspartners.modules.common.service.EventProducerService;
import com.obuspartners.modules.common.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Demo Controller for Kafka Event Testing
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/demo/kafka")
@RequiredArgsConstructor
@Tag(name = "Demo Kafka", description = "Demo endpoints for Kafka event testing")
public class DemoKafkaController {

    private final EventProducerService eventProducerService;

    /**
     * Send a demo event to Kafka
     */
    @PostMapping("/send-demo-event")
    @Operation(summary = "Send demo event to Kafka")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event sent successfully"),
        @ApiResponse(responseCode = "500", description = "Failed to send event")
    })
    public ResponseEntity<ResponseWrapper<String>> sendDemoEvent(
            @RequestBody Map<String, String> request) {
        
        try {
            String message = request.get("message");
            String source = request.getOrDefault("source", "DEMO_CONTROLLER");
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, 400, "Message is required", null));
            }
            
            log.info("Sending demo event: {}", message);
            eventProducerService.sendDemoEvent(message, source);
            
            return ResponseEntity.ok()
                .body(new ResponseWrapper<>(true, 200, "Demo event sent successfully", message));
                
        } catch (Exception e) {
            log.error("Error sending demo event", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseWrapper<>(false, 500, "Failed to send demo event: " + e.getMessage(), null));
        }
    }

    /**
     * Send a partner registered event to Kafka
     */
    @PostMapping("/send-partner-event")
    @Operation(summary = "Send partner registered event to Kafka")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event sent successfully"),
        @ApiResponse(responseCode = "500", description = "Failed to send event")
    })
    public ResponseEntity<ResponseWrapper<String>> sendPartnerEvent(
            @RequestBody Map<String, String> request) {
        
        try {
            String partnerId = request.get("partnerId");
            String partnerName = request.get("partnerName");
            
            if (partnerId == null || partnerName == null) {
                return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, 400, "Partner ID and name are required", null));
            }
            
            log.info("Sending partner registered event: {} - {}", partnerId, partnerName);
            eventProducerService.sendPartnerRegisteredEvent(partnerId, partnerName);
            
            return ResponseEntity.ok()
                .body(new ResponseWrapper<>(true, 200, "Partner event sent successfully", partnerName));
                
        } catch (Exception e) {
            log.error("Error sending partner event", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseWrapper<>(false, 500, "Failed to send partner event: " + e.getMessage(), null));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Kafka demo health check")
    public ResponseEntity<ResponseWrapper<String>> health() {
        return ResponseEntity.ok()
            .body(new ResponseWrapper<>(true, 200, "Kafka demo is healthy", "OK"));
    }
}
