package com.obuspartners.modules.partner_management.domain.enums;

/**
 * Partner tier enumeration
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public enum PartnerTier {
    BRONZE("BRONZE", "Bronze", "Entry-level partner tier with basic benefits", "#cd7f32"),
    SILVER("SILVER", "Silver", "Mid-level partner tier with enhanced benefits", "#c0c0c0"),
    GOLD("GOLD", "Gold", "Premium partner tier with advanced benefits", "#ffd700"),
    PLATINUM("PLATINUM", "Platinum", "High-tier partner with exclusive benefits", "#e5e4e2"),
    DIAMOND("DIAMOND", "Diamond", "Top-tier partner with maximum benefits", "#b9f2ff");

    private final String name;
    private final String displayName;
    private final String description;
    private final String colorCode;

    PartnerTier(String name, String displayName, String description, String colorCode) {
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
