package com.obuspartners.api.publicapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obuspartners.modules.partner_integration.mixx.entity.MixxPaymentCallback;
import com.obuspartners.modules.partner_integration.mixx.repository.MixxPaymentCallbackRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Controller for handling MIXX payment provider callbacks
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/public/mixx/v1")
@RequiredArgsConstructor
public class MixxPublicController {

    private final MixxPaymentCallbackRepository mixxPaymentCallbackRepository;

    /**
     * Handle MIXX payment callback
     * 
     * @param callbackRequest MIXX callback request
     * @return MIXX callback response
     */
    @PostMapping("/callback")
    public ResponseEntity<MixxCallbackResponse> handlePaymentCallback(
            @RequestBody MixxCallbackRequest callbackRequest) {
        
        log.info("Received MIXX payment callback: {}", callbackRequest);
        
        try {
            // Process the callback
            boolean isSuccess = processMixxCallback(callbackRequest);
            
            // Build response
            MixxCallbackResponse response = MixxCallbackResponse.builder()
                    .success(isSuccess)
                    .responseCode(isSuccess ? "BILLER-18-0000-S" : "BILLER-18-3020-E")
                    .transactionStatus(String.valueOf(isSuccess))
                    .errorDescription(isSuccess ? "Callback successful" : "Callback failed")
                    .referenceID(callbackRequest.getReferenceID())
                    .build();
            
            log.info("MIXX callback processed successfully: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing MIXX callback: {}", e.getMessage(), e);
            
            MixxCallbackResponse errorResponse = MixxCallbackResponse.builder()
                    .success(false)
                    .responseCode("BILLER-18-3020-E")
                    .transactionStatus("false")
                    .errorDescription("Callback processing failed: " + e.getMessage())
                    .referenceID(callbackRequest.getReferenceID())
                    .build();
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Process MIXX payment callback
     * 
     * @param callbackRequest MIXX callback request
     * @return true if successful, false otherwise
     */
    private boolean processMixxCallback(MixxCallbackRequest callbackRequest) {
        log.info("Processing MIXX callback for reference: {}", callbackRequest.getReferenceID());
        
        try {
            // Save callback data to database
            MixxPaymentCallback callback = new MixxPaymentCallback();
            callback.setAmount(new BigDecimal(callbackRequest.getAmount()));
            callback.setMfsTransactionId(callbackRequest.getMfsTransactionID());
            callback.setReferenceId(callbackRequest.getReferenceID());
            callback.setDescription(callbackRequest.getDescription());
            callback.setStatus(callbackRequest.getStatus());
            
            // Save to database
            MixxPaymentCallback savedCallback = mixxPaymentCallbackRepository.save(callback);
            log.info("MIXX callback saved successfully with UID: {}", savedCallback.getUid());
            
            // TODO: Implement actual callback processing logic later
            // This would typically involve:
            // 1. Find the booking by reference ID
            // 2. Update booking status based on payment result
            // 3. Publish PaymentCallbackEvent
            
            boolean isPaymentSuccessful = "true".equals(callbackRequest.getStatus());
            
            if (isPaymentSuccessful) {
                log.info("MIXX payment successful for reference: {}", callbackRequest.getReferenceID());
                // TODO: Update booking status to CONFIRMED
                // TODO: Publish PaymentCallbackEvent with SUCCESS status
            } else {
                log.info("MIXX payment failed for reference: {}", callbackRequest.getReferenceID());
                // TODO: Update booking status to FAILED
                // TODO: Publish PaymentCallbackEvent with FAILED status
            }
            
            return true; // Callback processing successful
            
        } catch (Exception e) {
            log.error("Error processing MIXX callback for reference: {}", 
                    callbackRequest.getReferenceID(), e);
            return false;
        }
    }

    /**
     * MIXX Callback Request DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MixxCallbackRequest {
        @JsonProperty("Amount")
        private String amount;
        
        @JsonProperty("MFSTransactionID")
        private String mfsTransactionID;
        
        @JsonProperty("ReferenceID")
        private String referenceID;
        
        @JsonProperty("Description")
        private String description;
        
        @JsonProperty("Status")
        private String status;
    }

    /**
     * MIXX Callback Response DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MixxCallbackResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("responseCode")
        private String responseCode;
        
        @JsonProperty("transactionStatus")
        private String transactionStatus;
        
        @JsonProperty("errorDescription")
        private String errorDescription;
        
        @JsonProperty("referenceID")
        private String referenceID;
    }
}
