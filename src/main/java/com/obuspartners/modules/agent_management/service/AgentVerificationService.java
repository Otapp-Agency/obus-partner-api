package com.obuspartners.modules.agent_management.service;

import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.partner_management.domain.entity.Partner;

import java.util.Map;
import java.util.Optional;

/**
 * Service interface for Agent verification operations with partner systems
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface AgentVerificationService {

    /**
     * Request agent verification with partner system
     * 
     * @param agentUid the agent UID
     * @param credentials the agent credentials for verification
     * @return verification request ID
     */
    String requestAgentVerification(String agentUid, Map<String, Object> credentials);

    /**
     * Get verification status for an agent
     * 
     * @param agentUid the agent UID
     * @return verification status information
     */
    Optional<VerificationStatus> getVerificationStatus(String agentUid);

    /**
     * Retry verification for an agent
     * 
     * @param agentUid the agent UID
     * @return true if retry was initiated successfully
     */
    boolean retryVerification(String agentUid);

    /**
     * Cancel verification request for an agent
     * 
     * @param agentUid the agent UID
     * @return true if cancellation was successful
     */
    boolean cancelVerification(String agentUid);

    /**
     * Test partner integration connection
     * 
     * @param partnerUid the partner UID
     * @return true if connection test successful
     */
    boolean testPartnerIntegration(String partnerUid);

    /**
     * Verify agent credentials with partner system
     * 
     * @param agent the agent to verify
     * @param partner the partner system
     * @param credentials the verification credentials
     * @return verification result
     */
    VerificationResult verifyAgentWithPartner(Agent agent, Partner partner, Map<String, Object> credentials);

    /**
     * Get supported credential fields for partner type
     * 
     * @param partnerUid the partner UID
     * @return map of supported credential fields and their descriptions
     */
    Map<String, String> getSupportedCredentials(String partnerUid);

    /**
     * Check if partner supports real-time verification
     * 
     * @param partnerUid the partner UID
     * @return true if partner supports real-time verification
     */
    boolean supportsRealTimeVerification(String partnerUid);

    /**
     * Get verification history for an agent
     * 
     * @param agentUid the agent UID
     * @return list of verification attempts
     */
    java.util.List<VerificationAttempt> getVerificationHistory(String agentUid);

    /**
     * Verification status enumeration
     */
    enum VerificationStatus {
        PENDING("PENDING", "Pending", "Verification is pending"),
        IN_PROGRESS("IN_PROGRESS", "In Progress", "Verification is in progress"),
        VERIFIED("VERIFIED", "Verified", "Verification was successful"),
        FAILED("FAILED", "Failed", "Verification failed"),
        REJECTED("REJECTED", "Rejected", "Verification was rejected"),
        TIMEOUT("TIMEOUT", "Timeout", "Verification timed out"),
        CANCELLED("CANCELLED", "Cancelled", "Verification was cancelled");

        private final String value;
        private final String displayName;
        private final String description;

        VerificationStatus(String value, String displayName, String description) {
            this.value = value;
            this.displayName = displayName;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public boolean isSuccessful() {
            return this == VERIFIED;
        }

        public boolean isInProgress() {
            return this == PENDING || this == IN_PROGRESS;
        }

        public boolean isFinal() {
            return this == VERIFIED || this == FAILED || this == REJECTED || 
                   this == TIMEOUT || this == CANCELLED;
        }
    }

    /**
     * Verification result class
     */
    class VerificationResult {
        private VerificationStatus status;
        private String message;
        private String verificationCode;
        private String partnerAgentId;
        private String partnerAgentCode;
        private Map<String, Object> partnerData;
        private String errorCode;
        private String errorMessage;
        private java.time.LocalDateTime verifiedAt;

        public VerificationResult() {}

        public VerificationResult(VerificationStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        // Getters and setters
        public VerificationStatus getStatus() { return status; }
        public void setStatus(VerificationStatus status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getVerificationCode() { return verificationCode; }
        public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

        public String getPartnerAgentId() { return partnerAgentId; }
        public void setPartnerAgentId(String partnerAgentId) { this.partnerAgentId = partnerAgentId; }

        public String getPartnerAgentCode() { return partnerAgentCode; }
        public void setPartnerAgentCode(String partnerAgentCode) { this.partnerAgentCode = partnerAgentCode; }

        public Map<String, Object> getPartnerData() { return partnerData; }
        public void setPartnerData(Map<String, Object> partnerData) { this.partnerData = partnerData; }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public java.time.LocalDateTime getVerifiedAt() { return verifiedAt; }
        public void setVerifiedAt(java.time.LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

        // Factory methods
        public static VerificationResult success(String message, String verificationCode) {
            VerificationResult result = new VerificationResult(VerificationStatus.VERIFIED, message);
            result.setVerificationCode(verificationCode);
            result.setVerifiedAt(java.time.LocalDateTime.now());
            return result;
        }

        public static VerificationResult failure(String errorCode, String errorMessage) {
            VerificationResult result = new VerificationResult(VerificationStatus.FAILED, errorMessage);
            result.setErrorCode(errorCode);
            return result;
        }

        public static VerificationResult rejected(String reason) {
            return new VerificationResult(VerificationStatus.REJECTED, reason);
        }

        public static VerificationResult timeout() {
            return new VerificationResult(VerificationStatus.TIMEOUT, "Verification request timed out");
        }
    }

    /**
     * Verification attempt class
     */
    class VerificationAttempt {
        private String requestId;
        private String agentUid;
        private String partnerUid;
        private VerificationStatus status;
        private String message;
        private String errorCode;
        private String errorMessage;
        private java.time.LocalDateTime requestedAt;
        private java.time.LocalDateTime completedAt;
        private Integer retryCount;

        public VerificationAttempt() {}

        // Getters and setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }

        public String getAgentUid() { return agentUid; }
        public void setAgentUid(String agentUid) { this.agentUid = agentUid; }

        public String getPartnerUid() { return partnerUid; }
        public void setPartnerUid(String partnerUid) { this.partnerUid = partnerUid; }

        public VerificationStatus getStatus() { return status; }
        public void setStatus(VerificationStatus status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public java.time.LocalDateTime getRequestedAt() { return requestedAt; }
        public void setRequestedAt(java.time.LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

        public java.time.LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(java.time.LocalDateTime completedAt) { this.completedAt = completedAt; }

        public Integer getRetryCount() { return retryCount; }
        public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    }
}
