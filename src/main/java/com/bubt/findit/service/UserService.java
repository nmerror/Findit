package com.bubt.findit.service;

import com.bubt.findit.core.event.AppEvent;
import com.bubt.findit.core.event.EventBus;
import com.bubt.findit.core.event.Events;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.Permission;
import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.model.UserStatus;
import com.bubt.findit.core.registry.ServiceRegistry;
import com.bubt.findit.core.spi.NotificationService;
import com.bubt.findit.db.dao.AuditDao;
import com.bubt.findit.db.dao.UserDao;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================================
 *  SEGMENT 1 - User Management Service
 * ============================================================================
 *
 * <p>Profile handling plus the administrator actions (suspend / restore /
 * change role). Segment 4 uses the same methods when it verifies a student.
 */
public class UserService {

    private final UserDao userDao;
    private final RbacService rbac;
    private final EventBus eventBus;
    private final AuditDao auditDao;

    public UserService(UserDao userDao, RbacService rbac, EventBus eventBus, AuditDao auditDao) {
        this.userDao = userDao;
        this.rbac = rbac;
        this.eventBus = eventBus;
        this.auditDao = auditDao;
    }

    /**
     * DELETE operation on the audit trail: keeps the log small by removing
     * the entries that are older than the given number of days.
     */
    public OperationResult<Integer> clearOldAuditEntries(User actor, int keepDays) {
        rbac.require(actor, Permission.MANAGE_USERS);
        if (keepDays < 1) {
            return OperationResult.fail("INVALID_RANGE", "Keep at least one day of history.");
        }
        int removed = auditDao.deleteOlderThan(LocalDateTime.now().minusDays(keepDays));
        audit(actor.getId(), "CLEAR_AUDIT_LOG", "Removed " + removed + " entries older than "
                + keepDays + " days");
        return OperationResult.ok(removed, removed + " old entries removed.");
    }

    public List<User> listUsers(User actor, int limit, int offset) {
        rbac.require(actor, Permission.MANAGE_USERS);
        return userDao.findAll(limit, offset);
    }

    public OperationResult<User> suspend(User actor, long userId) {
        rbac.require(actor, Permission.MANAGE_USERS);
        User target = userDao.findById(userId).orElse(null);
        if (target == null) {
            return OperationResult.fail("NOT_FOUND", "User not found.");
        }
        if (target.getId().equals(actor.getId())) {
            return OperationResult.fail("SELF_SUSPEND", "You cannot suspend your own account.");
        }
        if (!userDao.updateStatus(userId, UserStatus.SUSPENDED)) {
            return OperationResult.fail("UPDATE_FAILED", "Could not suspend the account.");
        }
        eventBus.publish(AppEvent.of(Events.USER_STATUS_CHANGED)
                .actor(actor.getId())
                .with("targetUserId", userId)
                .with("status", UserStatus.SUSPENDED.name())
                .build());
        notify(userId, "Account suspended",
                "Your FindIt account has been suspended. Please contact the administrator.");
        audit(actor.getId(), "SUSPEND_USER", "Suspended user " + userId);
        return OperationResult.ok(target, "Account suspended.");
    }

    public OperationResult<User> restore(User actor, long userId) {
        rbac.require(actor, Permission.MANAGE_USERS);
        User target = userDao.findById(userId).orElse(null);
        if (target == null) {
            return OperationResult.fail("NOT_FOUND", "User not found.");
        }
        userDao.updateStatus(userId, UserStatus.ACTIVE);
        eventBus.publish(AppEvent.of(Events.USER_STATUS_CHANGED)
                .actor(actor.getId())
                .with("targetUserId", userId)
                .with("status", UserStatus.ACTIVE.name())
                .build());
        notify(userId, "Account restored", "Your FindIt account is active again.");
        audit(actor.getId(), "RESTORE_USER", "Restored user " + userId);
        return OperationResult.ok(target, "Account restored.");
    }

    public OperationResult<User> changeRole(User actor, long userId, Role newRole) {
        rbac.require(actor, Permission.MANAGE_USERS);
        User target = userDao.findById(userId).orElse(null);
        if (target == null) {
            return OperationResult.fail("NOT_FOUND", "User not found.");
        }
        if (target.getId().equals(actor.getId())) {
            return OperationResult.fail("SELF_ROLE_CHANGE", "You cannot change your own role.");
        }
        userDao.updateRole(userId, newRole);
        eventBus.publish(AppEvent.of(Events.USER_ROLE_CHANGED)
                .actor(actor.getId())
                .with("targetUserId", userId)
                .with("role", newRole.name())
                .build());
        notify(userId, "Role changed", "Your role is now " + newRole.label() + ".");
        audit(actor.getId(), "CHANGE_ROLE", "User " + userId + " -> " + newRole.name());
        return OperationResult.ok(target, "Role updated.");
    }

    public OperationResult<User> updateProfile(User actor, long userId, String phone) {
        User target = userDao.findById(userId).orElse(null);
        if (target == null) {
            return OperationResult.fail("NOT_FOUND", "User not found.");
        }
        if (!actor.isAdmin() && !actor.getId().equals(userId)) {
            return OperationResult.fail("FORBIDDEN", "You can only edit your own profile.");
        }
        target.setPhone(phone);
        audit(actor.getId(), "UPDATE_PROFILE", "Updated profile of user " + userId);
        return OperationResult.ok(target.sanitized(), "Profile updated.");
    }

    private void notify(Long userId, String title, String message) {
        NotificationService notifications = ServiceRegistry.get(NotificationService.class);
        if (notifications != null) {
            notifications.notifyUser(userId, title, message);
        }
    }

    private void audit(Long actorUserId, String action, String details) {
        com.bubt.findit.core.spi.AuditService auditService =
                ServiceRegistry.get(com.bubt.findit.core.spi.AuditService.class);
        if (auditService != null) {
            auditService.record(action, actorUserId, details);
        }
    }
}
