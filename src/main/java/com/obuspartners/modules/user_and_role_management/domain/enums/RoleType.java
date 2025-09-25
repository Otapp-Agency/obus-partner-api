package com.obuspartners.modules.user_and_role_management.domain.enums;

/**
 * Role types enumeration for OBUS Partner API
 * Defines the available roles in the system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum RoleType {
    
    /**
     * Administrator role with full system access
     */
    ADMIN("ADMIN", "Administrator", "Full system access role"),
    
    /**
     * Manager role with management privileges
     */
    MANAGER("MANAGER", "Manager", "Management role with elevated privileges"),
    
    /**
     * User role with basic access
     */
    USER("USER", "User", "Basic user role with standard access"),
    
    /**
     * Partner role for partner-specific access
     */
    PARTNER("PARTNER", "Partner", "Partner role with partner-specific access"),
    
    /**
     * Agent role for operational tasks
     */
    AGENT("AGENT", "Agent", "Agent role for operational tasks"),
    
    /**
     * Guest role with limited access
     */
    GUEST("GUEST", "Guest", "Guest role with read-only access");
    
    private final String value;
    private final String displayName;
    private final String description;
    
    RoleType(String value, String displayName, String description) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Get the internal value of the role type
     * 
     * @return value of the role type
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Get the display name of the role type
     * 
     * @return display name of the role type
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the human-readable description of the role type
     * 
     * @return description of the role type
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this role type has administrative privileges
     * 
     * @return true if the role type has admin privileges
     */
    public boolean isAdmin() {
        return this == ADMIN || this == MANAGER;
    }
    
    /**
     * Check if this role type is a partner-related role
     * 
     * @return true if the role type is partner-related
     */
    public boolean isPartner() {
        return this == PARTNER || this == AGENT;
    }
}
