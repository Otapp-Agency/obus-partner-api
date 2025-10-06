package com.obuspartners.modules.partner_integration.mixx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for MIXX payment callback request
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MixxPaymentCallbackRequestDto {

    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("MFSTransactionID")
    private String mfsTransactionId;

    @JsonProperty("ReferenceID")
    private String referenceId;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Status")
    private String status;

    /**
     * Get amount as BigDecimal
     */
    public BigDecimal getAmountAsBigDecimal() {
        try {
            return new BigDecimal(this.amount);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Check if payment was successful
     */
    public boolean isPaymentSuccessful() {
        return "true".equals(this.status);
    }

    /**
     * Check if payment failed
     */
    public boolean isPaymentFailed() {
        return "false".equals(this.status);
    }
}
