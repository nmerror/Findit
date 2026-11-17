package com.bubt.findit.core.model;

/**
 * Item categories used for filtering and for the matching score (Segment 2
 * proposes the item, Segment 3 matches on category + description + image).
 */
public enum ItemCategory {

    ELECTRONICS("Electronics"),
    DOCUMENTS("Documents & Cards"),
    ACCESSORIES("Accessories"),
    CLOTHING("Clothing"),
    KEYS_CARDS("Keys & Cards"),
    BOOKS("Books & Stationery"),
    OTHER("Other");

    private final String label;

    ItemCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static ItemCategory fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        for (ItemCategory category : values()) {
            if (category.name().equalsIgnoreCase(value.trim())) {
                return category;
            }
        }
        return OTHER;
    }
}
