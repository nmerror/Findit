package com.bubt.findit.core.model;

/** Account lifecycle state. Owned by Segment 1. */
public enum UserStatus {

    ACTIVE("Active"),
    PENDING("Pending verification"),
    SUSPENDED("Suspended");

    private final String label;

    UserStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static UserStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE;
        }
        for (UserStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return ACTIVE;
    }
}
