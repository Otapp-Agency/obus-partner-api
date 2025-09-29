package com.obuspartners.modules.agent_management.domain.enums;

/**
 * Enumeration for agent verification status
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum AgentVerificationStatus {
    
    /**
     * Verification request is pending review
     */
    PENDING("PENDING", "Pending", "Verification request is pending review", "#FFA500"),
    
    /**
     * Verification request has been approved
     */
    APPROVED("APPROVED", "Approved", "Verification request has been approved", "#28A745"),
    
    /**
     * Verification request has been rejected
     */
    REJECTED("REJECTED", "Rejected", "Verification request has been rejected", "#DC3545"),
    
    /**
     * Verification request has been cancelled
     */
    CANCELLED("CANCELLED", "Cancelled", "Verification request has been cancelled", "#6C757D"),
    
    /**
     * Verification request has expired
     */
    EXPIRED("EXPIRED", "Expired", "Verification request has expired", "#FD7E14");

    private final String name;
    private final String displayName;
    private final String description;
    private final String colorCode;

    AgentVerificationStatus(String name, String displayName, String description, String colorCode) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getColorCode() {
        return colorCode;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
