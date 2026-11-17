package com.bubt.findit.core.model;

/**
 * Roles in FindIt. Owned by Segment 1 (User Authentication, Role-Based Access
 * Control & Dashboard Management).
 *
 * <p>Other segments must reference this enum and never re-declare their own
 * role constants, otherwise the FK from items/claims to users becomes ambiguous.
 */
public enum Role {

    STUDENT("Student"),
    ADMIN("Administrator");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Lenient parse used when reading a value from the database. */
    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            return STUDENT;
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        return STUDENT;
    }
}
