package com.obuspartners.modules.partner_management.domain.enums;

/**
 * Partner type enumeration
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum PartnerType {
    INDIVIDUAL("INDIVIDUAL", "Individual", "Individual or sole proprietor partner", "#007bff"),
    CORPORATE("CORPORATE", "Corporate", "Corporate business entity partner", "#6610f2"),
    ENTERPRISE("ENTERPRISE", "Enterprise", "Large enterprise partner", "#e83e8c"),
    GOVERNMENT("GOVERNMENT", "Government", "Government or public sector partner", "#fd7e14"),
    NON_PROFIT("NON_PROFIT", "Non-Profit", "Non-profit organization partner", "#20c997");

    private final String name;
    private final String displayName;
    private final String description;
    private final String colorCode;

    PartnerType(String name, String displayName, String description, String colorCode) {
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
}
