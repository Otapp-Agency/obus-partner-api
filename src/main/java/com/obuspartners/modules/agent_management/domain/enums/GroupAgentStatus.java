package com.obuspartners.modules.agent_management.domain.enums;

/**
 * GroupAgent status enumeration for OBUS Partner API
 * Defines the different statuses of group agents in the system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum GroupAgentStatus {
    
    /**
     * Group agent is active and operational
     */
    ACTIVE("ACTIVE", "Active", "Group agent is active and operational", "#28A745"),
    
    /**
     * Group agent is suspended temporarily
     */
    SUSPENDED("SUSPENDED", "Suspended", "Group agent is suspended temporarily", "#FFC107"),
    
    /**
     * Group agent is inactive
     */
    INACTIVE("INACTIVE", "Inactive", "Group agent is inactive", "#6C757D"),
    
    /**
     * Group agent is pending approval
     */
    PENDING_APPROVAL("PENDING_APPROVAL", "Pending Approval", "Group agent is pending approval", "#17A2B8"),
    
    /**
     * Group agent is rejected
     */
    REJECTED("REJECTED", "Rejected", "Group agent is rejected", "#DC3545"),
    
    /**
     * Group agent is under review
     */
    UNDER_REVIEW("UNDER_REVIEW", "Under Review", "Group agent is under review", "#FD7E14");
    
    private final String value;
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    GroupAgentStatus(String value, String displayName, String description, String colorCode) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }
    
    /**
     * Get the internal value of the group agent status
     * 
     * @return value of the group agent status
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Get the display name of the group agent status
     * 
     * @return display name of the group agent status
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the human-readable description of the group agent status
     * 
     * @return description of the group agent status
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the color code for UI display
     * 
     * @return color code (hex format)
     */
    public String getColorCode() {
        return colorCode;
    }
    
    /**
     * Check if this group agent status is active
     * 
     * @return true if the group agent status is active
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * Check if this group agent status is operational
     * 
     * @return true if the group agent status allows operations
     */
    public boolean isOperational() {
        return this == ACTIVE;
    }
    
    /**
     * Check if this group agent status is suspended
     * 
     * @return true if the group agent status is suspended
     */
    public boolean isSuspended() {
        return this == SUSPENDED;
    }
    
    /**
     * Check if this group agent status is inactive
     * 
     * @return true if the group agent status is inactive
     */
    public boolean isInactive() {
        return this == INACTIVE;
    }
    
    /**
     * Check if this group agent status is pending
     * 
     * @return true if the group agent status is pending
     */
    public boolean isPending() {
        return this == PENDING_APPROVAL || this == UNDER_REVIEW;
    }
    
    /**
     * Check if this group agent status is rejected
     * 
     * @return true if the group agent status is rejected
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
}
