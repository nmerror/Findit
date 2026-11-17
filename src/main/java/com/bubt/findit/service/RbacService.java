package com.bubt.findit.service;

import com.bubt.findit.core.model.Permission;
import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;

/**
 * Role-based access control. Segment 1 owns this service.
 *
 * <p>Segments 2, 3 and 4 must guard their actions with this service instead
 * of comparing roles themselves. One call:
 *
 * <pre>{@code
 * rbac.require(currentUser, Permission.APPROVE_CLAIM);
 * }</pre>
 */
public class RbacService {

    /** Is the user allowed to perform this action? */
    public boolean can(User user, Permission permission) {
        if (user == null || permission == null) {
            return false;
        }
        if (user.getStatus() != com.bubt.findit.core.model.UserStatus.ACTIVE) {
            return false;
        }
        return permission.grantedTo(user.getRole());
    }

    /** Same as {@link #can} but throws when the action is not allowed. */
    public void require(User user, Permission permission) {
        if (!can(user, permission)) {
            String name = user == null ? "anonymous user" : user.getFullName();
            throw new AccessDeniedException(name + " is not allowed to: " + permission.label());
        }
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    /** All permissions the given user holds - used to build the navigation. */
    public java.util.List<Permission> permissionsOf(User user) {
        java.util.List<Permission> granted = new java.util.ArrayList<>();
        if (user == null) {
            return granted;
        }
        for (Permission permission : Permission.values()) {
            if (can(user, permission)) {
                granted.add(permission);
            }
        }
        return granted;
    }
}
