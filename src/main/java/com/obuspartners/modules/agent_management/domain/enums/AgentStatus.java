package com.obuspartners.modules.agent_management.domain.enums;

/**
 * Agent status enumeration for OBUS Partner API
 * Defines the various statuses an agent can have in the system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum AgentStatus {
    
    /**
     * Agent registration pending approval
     */
    PENDING_APPROVAL("PENDING_APPROVAL", "Pending Approval", "Agent registration is pending approval", "#FFA500"),
    
    /**
     * Agent is active and operational
     */
    ACTIVE("ACTIVE", "Active", "Agent is active and operational", "#28A745"),
    
    /**
     * Agent is temporarily suspended
     */
    SUSPENDED("SUSPENDED", "Suspended", "Agent is temporarily suspended", "#FFC107"),
    
    /**
     * Agent is inactive (deactivated)
     */
    INACTIVE("INACTIVE", "Inactive", "Agent is inactive", "#6C757D"),
    
    /**
     * Agent registration was rejected
     */
    REJECTED("REJECTED", "Rejected", "Agent registration was rejected", "#DC3545"),
    
    /**
     * Agent account is locked due to security issues
     */
    LOCKED("LOCKED", "Locked", "Agent account is locked", "#DC3545");
    
    private final String value;
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    AgentStatus(String value, String displayName, String description, String colorCode) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }
    
    /**
     * Get the internal value of the agent status
     * 
     * @return value of the agent status
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Get the display name of the agent status
     * 
     * @return display name of the agent status
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the human-readable description of the agent status
     * 
     * @return description of the agent status
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
     * Check if this status allows agent to perform operations
     * 
     * @return true if the status allows operations
     */
    public boolean allowsOperations() {
        return this == ACTIVE;
    }
    
    /**
     * Check if this status indicates the agent is approved
     * 
     * @return true if the status indicates approval
     */
    public boolean isApproved() {
        return this == ACTIVE || this == SUSPENDED;
    }
    
    /**
     * Check if this status indicates the agent is pending
     * 
     * @return true if the status indicates pending state
     */
    public boolean isPending() {
        return this == PENDING_APPROVAL;
    }
    
    /**
     * Check if this status indicates the agent is inactive
     * 
     * @return true if the status indicates inactive state
     */
    public boolean isInactive() {
        return this == INACTIVE || this == REJECTED || this == LOCKED;
    }
}
