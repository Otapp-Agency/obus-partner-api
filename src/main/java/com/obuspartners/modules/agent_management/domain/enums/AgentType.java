package com.obuspartners.modules.agent_management.domain.enums;

/**
 * Agent type enumeration for OBUS Partner API
 * Defines the different types of agents in the system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum AgentType {
    
    /**
     * Individual agent (person)
     */
    INDIVIDUAL("INDIVIDUAL", "Individual", "Individual agent (person)", "#17A2B8"),
    
    /**
     * Corporate agent (company/organization)
     */
    CORPORATE("CORPORATE", "Corporate", "Corporate agent (company/organization)", "#6F42C1"),
    
    /**
     * Franchise agent
     */
    FRANCHISE("FRANCHISE", "Franchise", "Franchise agent", "#20C997"),
    
    /**
     * Sub-agent (under another agent)
     */
    SUB_AGENT("SUB_AGENT", "Sub-Agent", "Sub-agent (under another agent)", "#FD7E14");
    
    private final String value;
    private final String displayName;
    private final String description;
    private final String colorCode;
    
    AgentType(String value, String displayName, String description, String colorCode) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
    }
    
    /**
     * Get the internal value of the agent type
     * 
     * @return value of the agent type
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Get the display name of the agent type
     * 
     * @return display name of the agent type
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the human-readable description of the agent type
     * 
     * @return description of the agent type
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
     * Check if this agent type is individual
     * 
     * @return true if the agent type is individual
     */
    public boolean isIndividual() {
        return this == INDIVIDUAL;
    }
    
    /**
     * Check if this agent type is corporate
     * 
     * @return true if the agent type is corporate
     */
    public boolean isCorporate() {
        return this == CORPORATE || this == FRANCHISE;
    }
    
    /**
     * Check if this agent type requires business registration
     * 
     * @return true if the agent type requires business registration
     */
    public boolean requiresBusinessRegistration() {
        return this == CORPORATE || this == FRANCHISE;
    }
}
