package com.obuspartners.modules.agent_management.domain.enums;

/**
 * GroupAgent type enumeration for OBUS Partner API
 * Defines the different types of group agents in the system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum GroupAgentType {
    
    /**
     * Standard group agent for regular business operations
     */
    STANDARD("STANDARD", "Standard", "Standard group agent for regular business operations", "#17A2B8"),
    
    /**
     * Premium group agent with enhanced features
     */
    PREMIUM("PREMIUM", "Premium", "Premium group agent with enhanced features", "#28A745"),
    
    /**
     * Enterprise group agent for large operations
     */
    ENTERPRISE("ENTERPRISE", "Enterprise", "Enterprise group agent for large operations", "#6F42C1"),
    
    /**
     * Franchise group agent
     */
    FRANCHISE("FRANCHISE", "Franchise", "Franchise group agent", "#20C997"),
    
    /**
     * Corporate group agent
     */
    CORPORATE("CORPORATE", "Corporate", "Corporate group agent", "#007BFF");
    
    private final String value;
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    GroupAgentType(String value, String displayName, String description, String colorCode) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }
    
    /**
     * Get the internal value of the group agent type
     * 
     * @return value of the group agent type
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Get the display name of the group agent type
     * 
     * @return display name of the group agent type
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the human-readable description of the group agent type
     * 
     * @return description of the group agent type
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
     * Check if this group agent type is standard
     * 
     * @return true if the group agent type is standard
     */
    public boolean isStandard() {
        return this == STANDARD;
    }
    
    /**
     * Check if this group agent type is premium
     * 
     * @return true if the group agent type is premium
     */
    public boolean isPremium() {
        return this == PREMIUM;
    }
    
    /**
     * Check if this group agent type is enterprise
     * 
     * @return true if the group agent type is enterprise
     */
    public boolean isEnterprise() {
        return this == ENTERPRISE;
    }
    
    /**
     * Check if this group agent type is corporate
     * 
     * @return true if the group agent type is corporate
     */
    public boolean isCorporate() {
        return this == CORPORATE || this == FRANCHISE;
    }
    
    /**
     * Check if this group agent type requires business registration
     * 
     * @return true if the group agent type requires business registration
     */
    public boolean requiresBusinessRegistration() {
        return this == CORPORATE || this == FRANCHISE || this == ENTERPRISE;
    }
}
