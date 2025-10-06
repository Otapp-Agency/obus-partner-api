package com.obuspartners.modules.booking_management.service;

import com.obuspartners.modules.booking_management.domain.event.BookingPaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for consuming payment events from Kafka topics
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final RestTemplate restTemplate = new RestTemplate();

    //https://a73b4e89473e.ngrok-free.app
    @Value("${mixx.payment.api.url:https://a73b4e89473e.ngrok-free.app}")
    //@Value("${mixx.payment.api.url:https://accessgwtest.tigo.co.tz:8443/api/v1/payment}")
    private String mixxPaymentApiUrl;

    @Value("${mixx.payment.api.key:c3VwZXJhcHByMnAtMUQkVSZsY0hhWEI3a0IhT2hSMUpLQnEyZDVCT1VUbmRmMkVwUGRWUG9EZ0tFa0do}")
    //@Value("${mixx.payment.api.key:c3VwZXJhcHByMnAtMUQkVSZsY0hhWEI3a0IhT2hSMUpLQnEyZDVCT1VUbmRmMkVwUGRWUG9EZ0tFa0do}")
    private String mixxApiKey;

    @Value("${mixx.payment.user.id:otappagent}")
    //@Value("${mixx.payment.user.id:UserId}")
    private String mixxUserId;

    @Value("${mixx.payment.biller.msisdn:25565150888}")
    private String billerMsisdn;

    /**
     * Consume booking payment events from the obus.booking.payment topic
     */
    @KafkaListener(
        topics = "obus.booking.payment",
        groupId = "obus-payment-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeBookingPaymentEvent(
            @Payload BookingPaymentEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received booking payment event: {} from topic: {}, partition: {}, offset: {}", 
                    event.getEventId(), topic, partition, offset);
            log.info("Booking UID: {}, Amount: {} {}, Payment Provider: {}", 
                    event.getBookingUid(), event.getAmount(), event.getCurrency(), event.getPaymentProvider());
            
            // Process the payment event
            processBookingPaymentEvent(event);
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
            log.info("Successfully processed booking payment event: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process booking payment event: {}", event.getEventId(), e);
            // Don't acknowledge on error - message will be retried
        }
    }

    /**
     * Process booking payment event
     */
    private void processBookingPaymentEvent(BookingPaymentEvent event) {
        log.info("Processing payment event for booking UID: {}", event.getBookingUid());
        
        try {
            // Route to appropriate payment provider
            switch (event.getPaymentProvider().toUpperCase()) {
                case "MIXX":
                    processMixxPayment(event);
                    break;
                case "BMSLG":
                    processBmslgPayment(event);
                    break;
                case "CASH":
                    processCashPayment(event);
                    break;
                default:
                    log.warn("Unknown payment provider: {}", event.getPaymentProvider());
                    processCashPayment(event); // Default to cash
            }
            
            log.info("Payment event processed successfully for booking UID: {}", event.getBookingUid());
            
        } catch (Exception e) {
            log.error("Error processing payment event for booking UID: {}", event.getBookingUid(), e);
            throw e; // Re-throw to trigger retry logic
        }
    }

    /**
     * Process MIXX payment
     */
    private void processMixxPayment(BookingPaymentEvent event) {
        log.info("Processing MIXX payment for booking UID: {}", event.getBookingUid());
        
        try {
            // Prepare MIXX payment request
            Map<String, Object> paymentRequest = new HashMap<>();
            paymentRequest.put("CustomerMSISDN", event.getCustomerPhone());
            paymentRequest.put("BillerMSISDN", billerMsisdn);
            // Convert amount based on currency (TZS doesn't have cents, USD does)
            int amountInSmallestUnit = event.getCurrency().equals("TZS") 
                ? event.getAmount().intValue()  // TZS: use amount as-is
                : event.getAmount().multiply(new BigDecimal("100")).intValue(); // USD: convert to cents
            paymentRequest.put("Amount", amountInSmallestUnit);
            paymentRequest.put("ReferenceID", event.getPaymentProviderReference());

            log.info("Amount conversion - Original: {} {}, Converted: {} (currency: {})", 
                    event.getAmount(), event.getCurrency(), amountInSmallestUnit, event.getCurrency());
            log.info("MIXX payment request: {}", paymentRequest);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("X-API-Key", mixxApiKey);
            headers.set("X-User-Id", mixxUserId);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(paymentRequest, headers);

            // Call MIXX API
            log.info("Calling MIXX payment API: {}", mixxPaymentApiUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    mixxPaymentApiUrl, 
                    requestEntity, 
                    Map.class
            );

            log.info("MIXX API response status: {}", response.getStatusCode());
            log.info("MIXX API response body: {}", response.getBody());

            // Process response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                String responseCode = (String) responseBody.get("ResponseCode");
                Boolean responseStatus = (Boolean) responseBody.get("ResponseStatus");
                String responseDescription = (String) responseBody.get("ResponseDescription");
                String referenceId = (String) responseBody.get("ReferenceID");

                log.info("MIXX payment response - Code: {}, Status: {}, Description: {}, Reference: {}", 
                        responseCode, responseStatus, responseDescription, referenceId);

                if (Boolean.TRUE.equals(responseStatus)) {
                    log.info("MIXX payment request sent successfully for booking UID: {}", event.getBookingUid());
                    // Payment request sent successfully, customer will receive PIN prompt
                } else {
                    log.error("MIXX payment request failed for booking UID: {} - {}", 
                            event.getBookingUid(), responseDescription);
                    // Handle payment request failure
                }
            } else {
                log.error("MIXX API call failed with status: {} for booking UID: {}", 
                        response.getStatusCode(), event.getBookingUid());
            }

        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("MIXX API server error for booking UID: {} - Status: {}, Response: {}", 
                    event.getBookingUid(), e.getStatusCode(), e.getResponseBodyAsString());
            // Don't rethrow - log the error and continue
            log.warn("MIXX payment API is currently unavailable. Payment request failed for booking UID: {}", 
                    event.getBookingUid());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("MIXX API client error for booking UID: {} - Status: {}, Response: {}", 
                    event.getBookingUid(), e.getStatusCode(), e.getResponseBodyAsString());
            // Don't rethrow - log the error and continue
            log.warn("MIXX payment API request was invalid. Payment request failed for booking UID: {}", 
                    event.getBookingUid());
        } catch (Exception e) {
            log.error("Error processing MIXX payment for booking UID: {}", event.getBookingUid(), e);
            throw e;
        }
    }

    /**
     * Process BMSLG payment
     */
    private void processBmslgPayment(BookingPaymentEvent event) {
        log.info("Processing BMSLG payment for booking UID: {}", event.getBookingUid());
        
        try {
            // TODO: Implement BMSLG payment integration
            log.info("BMSLG payment initiated for booking UID: {}", event.getBookingUid());
            
            // Simulate payment processing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Payment processing interrupted", e);
            }
            
            log.info("BMSLG payment processing completed for booking UID: {}", event.getBookingUid());
            
        } catch (Exception e) {
            log.error("Error processing BMSLG payment for booking UID: {}", event.getBookingUid(), e);
            throw e;
        }
    }

    /**
     * Process cash payment
     */
    private void processCashPayment(BookingPaymentEvent event) {
        log.info("Processing cash payment for booking UID: {}", event.getBookingUid());
        
        try {
            // Cash payments are typically handled at the point of service
            // This might involve:
            // 1. Marking payment as pending
            // 2. Sending instructions to agent
            // 3. Waiting for manual confirmation
            
            log.info("Cash payment marked as pending for booking UID: {}", event.getBookingUid());
            
            // Simulate cash payment processing
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Payment processing interrupted", e);
            }
            
            log.info("Cash payment processing completed for booking UID: {}", event.getBookingUid());
            
        } catch (Exception e) {
            log.error("Error processing cash payment for booking UID: {}", event.getBookingUid(), e);
            throw e;
        }
    }
}
