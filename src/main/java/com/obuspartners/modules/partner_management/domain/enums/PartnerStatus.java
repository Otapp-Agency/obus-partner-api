package com.obuspartners.modules.partner_management.domain.enums;

/**
 * Partner status enumeration
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum PartnerStatus {
    ACTIVE("ACTIVE", "Active", "Partner is active and operational", "#28a745"),
    INACTIVE("INACTIVE", "Inactive", "Partner is temporarily inactive", "#6c757d"),
    SUSPENDED("SUSPENDED", "Suspended", "Partner account is suspended", "#ffc107"),
    PENDING_VERIFICATION("PENDING_VERIFICATION", "Pending Verification", "Partner is awaiting verification", "#17a2b8"),
    REJECTED("REJECTED", "Rejected", "Partner application was rejected", "#dc3545"),
    TERMINATED("TERMINATED", "Terminated", "Partner account has been terminated", "#343a40");

    private final String name;
    private final String displayName;
    private final String description;
    private final String colorCode;

    PartnerStatus(String name, String displayName, String description, String colorCode) {
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
