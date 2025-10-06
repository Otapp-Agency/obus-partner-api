package com.obuspartners.modules.partner_integration.mixx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for MIXX payment callback response
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MixxPaymentCallbackResponseDto {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("responseCode")
    private String responseCode;

    @JsonProperty("transactionStatus")
    private String transactionStatus;

    @JsonProperty("errorDescription")
    private String errorDescription;

    @JsonProperty("referenceID")
    private String referenceId;

    /**
     * Create success response
     */
    public static MixxPaymentCallbackResponseDto success(String referenceId) {
        return MixxPaymentCallbackResponseDto.builder()
                .success(true)
                .responseCode("BILLER-18-0000-S")
                .transactionStatus("true")
                .errorDescription("Callback successful")
                .referenceId(referenceId)
                .build();
    }

    /**
     * Create failure response
     */
    public static MixxPaymentCallbackResponseDto failure(String referenceId, String errorDescription) {
        return MixxPaymentCallbackResponseDto.builder()
                .success(false)
                .responseCode("BILLER-18-3020-E")
                .transactionStatus("false")
                .errorDescription(errorDescription)
                .referenceId(referenceId)
                .build();
    }

    /**
     * Create failure response with default error message
     */
    public static MixxPaymentCallbackResponseDto failure(String referenceId) {
        return failure(referenceId, "Callback failed");
    }
}
