package com.obuspartners.modules.user_and_role_management.domain.enums;

/**
 * User status enumeration for OBUS Partner API
 * Defines the various statuses a system user can have
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum UserStatus {
    
    /**
     * User is active and operational
     */
    ACTIVE("ACTIVE", "Active", "User is active and operational", "#28A745"),
    
    /**
     * User is inactive (deactivated)
     */
    INACTIVE("INACTIVE", "Inactive", "User is inactive", "#6C757D"),
    
    /**
     * User is temporarily suspended
     */
    SUSPENDED("SUSPENDED", "Suspended", "User is temporarily suspended", "#FFC107"),
    
    /**
     * User account is pending verification
     */
    PENDING_VERIFICATION("PENDING_VERIFICATION", "Pending Verification", "User account is pending verification", "#FFA500"),
    
    /**
     * User account is locked due to security issues
     */
    LOCKED("LOCKED", "Locked", "User account is locked", "#DC3545");
    
    private final String value;
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    UserStatus(String value, String displayName, String description, String colorCode) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }
    
    /**
     * Get the internal value of the user status
     * 
     * @return value of the user status
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Get the display name of the user status
     * 
     * @return display name of the user status
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the human-readable description of the user status
     * 
     * @return description of the user status
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
     * Check if this status allows user to perform operations
     * 
     * @return true if the status allows operations
     */
    public boolean allowsOperations() {
        return this == ACTIVE;
    }
    
    /**
     * Check if this status indicates the user is active
     * 
     * @return true if the status indicates active state
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * Check if this status indicates the user is pending
     * 
     * @return true if the status indicates pending state
     */
    public boolean isPending() {
        return this == PENDING_VERIFICATION;
    }
    
    /**
     * Check if this status indicates the user is inactive
     * 
     * @return true if the status indicates inactive state
     */
    public boolean isInactive() {
        return this == INACTIVE || this == LOCKED;
    }
}
