package com.obuspartners.modules.user_and_role_management.domain.enums;

/**
 * User types enumeration for OBUS Partner API
 * Defines the three main user types in the system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum UserType {
    
    /**
     * Administrative user with full system access
     */
    ADMIN_USER("ADMIN_USER", "Admin User", "Administrative user with full system access", "#FF6B6B"),
    
    /**
     * Partner user with partner-specific access
     */
    PARTNER_USER("PARTNER_USER", "Partner User", "Partner user with partner-specific access", "#4ECDC4"),
    
    /**
     * Agent user with limited access for operational tasks
     */
    AGENT("AGENT", "Agent", "Agent user with limited access for operational tasks", "#45B7D1");
    
    private final String name;
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    UserType(String name, String displayName, String description, String colorCode) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }
    
    /**
     * Get the internal name of the user type
     * 
     * @return name of the user type
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the display name of the user type
     * 
     * @return display name of the user type
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the human-readable description of the user type
     * 
     * @return description of the user type
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
     * Check if this user type has administrative privileges
     * 
     * @return true if the user type has admin privileges
     */
    public boolean isAdmin() {
        return this == ADMIN_USER;
    }
    
    /**
     * Check if this user type is a partner-related user
     * 
     * @return true if the user type is partner-related
     */
    public boolean isPartner() {
        return this == PARTNER_USER || this == AGENT;
    }
    
}
