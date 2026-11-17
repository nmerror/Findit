package com.bubt.findit.core.model;

/**
 * Every capability in FindIt is expressed as a permission instead of a raw
 * role check. Segment 1 owns this enum; Segments 2, 3 and 4 call
 * {@code RbacService.require(user, Permission.X)} instead of
 * {@code if (user.getRole() == ADMIN)}.
 *
 * <p>Why this matters for integration: if a new role is added later (e.g.
 * DEPARTMENT_ADMIN) you only change this one file, not every screen that used
 * to compare roles directly.
 */
public enum Permission {

    REPORT_ITEM("Report a lost or found item"),
    VIEW_ALL_ITEMS("Browse all items"),
    UPDATE_OWN_REPORT("Update own report"),
    SUBMIT_CLAIM("Submit an ownership claim"),
    VIEW_OWN_CLAIM("View own claims"),
    RUN_MATCHING("Trigger item matching"),
    VERIFY_CLAIM("Verify claim evidence"),
    APPROVE_CLAIM("Approve or reject a claim"),
    MARK_RETURNED("Confirm an item as returned"),
    VIEW_ADMIN_DASHBOARD("View the admin dashboard"),
    MANAGE_USERS("Suspend or restore user accounts");

    private final String label;

    Permission(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /**
     * Default role-to-permission mapping. The single place where "who can do
     * what" is decided.
     */
    public boolean grantedTo(Role role) {
        if (role == null) {
            return false;
        }
        switch (this) {
            case RUN_MATCHING:
            case VERIFY_CLAIM:
            case APPROVE_CLAIM:
            case MARK_RETURNED:
            case VIEW_ADMIN_DASHBOARD:
            case MANAGE_USERS:
                return role == Role.ADMIN;
            default:
                return true;
        }
    }
}
