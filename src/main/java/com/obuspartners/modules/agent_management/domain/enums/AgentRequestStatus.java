package com.obuspartners.modules.agent_management.domain.enums;

/**
 * Agent Request Status Enum
 * Represents the status of an agent registration request
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum AgentRequestStatus {
    
    PENDING("PENDING", "Pending", "Request is pending verification", "#FFA500"),
    APPROVED("APPROVED", "Approved", "Request approved and agent created", "#28A745"),
    REJECTED("REJECTED", "Rejected", "Request rejected due to verification failure", "#DC3545"),
    CANCELLED("CANCELLED", "Cancelled", "Request cancelled by user or system", "#6C757D"),
    EXPIRED("EXPIRED", "Expired", "Request expired without verification", "#6C757D");
    
    private final String name;
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    AgentRequestStatus(String name, String displayName, String description, String colorCode) {
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
    
    public boolean isPending() {
        return this == PENDING;
    }
    
    public boolean isApproved() {
        return this == APPROVED;
    }
    
    public boolean isRejected() {
        return this == REJECTED;
    }
    
    public boolean isCancelled() {
        return this == CANCELLED;
    }
    
    public boolean isExpired() {
        return this == EXPIRED;
    }
    
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED || this == CANCELLED || this == EXPIRED;
    }
    
    public static AgentRequestStatus fromName(String name) {
        for (AgentRequestStatus status : values()) {
            if (status.name.equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AgentRequestStatus: " + name);
    }
}
