package com.bubt.findit.core.model;

/**
 * LOST or FOUND. Declared in core (not in Segment 2's package) because
 * Segment 1's dashboard has to count both kinds of items, and Segment 3's
 * matching engine compares a LOST item against a FOUND item.
 */
public enum ItemType {

    LOST("Lost Item"),
    FOUND("Found Item");

    private final String label;

    ItemType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static ItemType fromString(String value) {
        if (value == null || value.isBlank()) {
            return LOST;
        }
        for (ItemType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return LOST;
    }
}
