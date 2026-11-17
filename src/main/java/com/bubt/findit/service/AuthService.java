package com.bubt.findit.service;

import com.bubt.findit.core.AuthContext;
import com.bubt.findit.core.event.AppEvent;
import com.bubt.findit.core.event.EventBus;
import com.bubt.findit.core.event.Events;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.Session;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.model.UserStatus;
import com.bubt.findit.core.registry.ServiceRegistry;
import com.bubt.findit.core.spi.NotificationService;
import com.bubt.findit.core.util.PasswordHasher;
import com.bubt.findit.core.util.Validation;
import com.bubt.findit.db.dao.UserDao;

import java.time.LocalDateTime;

/**
 * ============================================================================
 *  SEGMENT 1 - Authentication Service
 * ============================================================================
 *
 * <p>Handles registration, login, logout and password change. This is the
 * only place that touches password material.
 *
 * <p>Integration notes for the other segments:
 * <ul>
 *   <li>They never call {@code AuthService} directly - they read the current
 *       user from {@code AuthContext.currentUser()}.</li>
 *   <li>They react to {@link Events#USER_REGISTERED} / {@code USER_LOGIN_SUCCESS}
 *       through the {@link EventBus} if they need to.</li>
 *   <li>Every login and registration is written to the audit log.</li>
 * </ul>
 */
public class AuthService {

    private static final String ERR_EMAIL_EXISTS = "EMAIL_EXISTS";
    private static final String ERR_STUDENT_ID_EXISTS = "STUDENT_ID_EXISTS";
    private static final String ERR_INVALID_EMAIL = "INVALID_EMAIL";
    private static final String ERR_INVALID_STUDENT_ID = "INVALID_STUDENT_ID";
    private static final String ERR_WEAK_PASSWORD = "WEAK_PASSWORD";
    private static final String ERR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    private static final String ERR_ACCOUNT_SUSPENDED = "ACCOUNT_SUSPENDED";
    private static final String ERR_NOT_FOUND = "NOT_FOUND";

    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final EventBus eventBus;

    public AuthService(UserDao userDao, SessionManager sessionManager, EventBus eventBus) {
        this.userDao = userDao;
        this.sessionManager = sessionManager;
        this.eventBus = eventBus;
    }

    /**
     * Registers a new student account. Admin accounts are NOT created here -
     * they are seeded into the database (see sql/02_seed_data.sql) so that
     * nobody can promote themselves by registering.
     */
    public OperationResult<User> register(String fullName, String email, String studentId,
                                          String password, String confirmPassword) {
        String name = fullName == null ? "" : fullName.trim();
        if (name.isEmpty()) {
            return OperationResult.fail("NAME_REQUIRED", "Full name is required.");
        }
        if (!Validation.isValidEmail(email)) {
            return OperationResult.fail(ERR_INVALID_EMAIL, "Enter a valid e-mail address.");
        }
        if (!Validation.isValidStudentId(studentId)) {
            return OperationResult.fail(ERR_INVALID_STUDENT_ID,
                    "Student ID must be 4-20 letters or digits (for example 21-04234).");
        }
        if (!Validation.isStrongPassword(password)) {
            return OperationResult.fail(ERR_WEAK_PASSWORD,
                    "Password needs at least 8 characters and must contain a letter and a digit.");
        }
        if (password == null || !password.equals(confirmPassword)) {
            return OperationResult.fail("PASSWORD_MISMATCH", "The two passwords do not match.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (userDao.existsByEmail(normalizedEmail)) {
            return OperationResult.fail(ERR_EMAIL_EXISTS, "That e-mail address is already registered.");
        }
        if (userDao.findByStudentId(studentId.trim()).isPresent()) {
            return OperationResult.fail(ERR_STUDENT_ID_EXISTS, "That student ID is already registered.");
        }

        PasswordHasher.HashedPassword hashed = PasswordHasher.hash(password.toCharArray());
        User user = User.create(name, normalizedEmail, studentId.trim(),
                hashed.getHash(), hashed.getSalt(), Role.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        User saved = userDao.insert(user);

        eventBus.publish(AppEvent.of(Events.USER_REGISTERED)
                .actor(saved.getId())
                .with("email", saved.getEmail())
                .with("fullName", saved.getFullName())
                .build());

        notify(saved.getId(), "Welcome to FindIt",
                "Your account is ready. Report a lost item to get started.");
        audit(saved.getId(), "REGISTER", "New student account: " + saved.getEmail());

        return OperationResult.ok(saved.sanitized(), "Account created. You can log in now.");
    }

    /**
     * Logs a user in. The identifier can be either the e-mail address or the
     * student ID - students usually remember one and not the other.
     */
    public OperationResult<Session> login(String identifier, String password) {
        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            return OperationResult.fail(ERR_INVALID_CREDENTIALS, "Enter your e-mail / student ID and password.");
        }
        String trimmed = identifier.trim();
        User user = userDao.findByEmail(trimmed.toLowerCase())
                .or(() -> userDao.findByStudentId(trimmed))
                .orElse(null);

        if (user == null || !PasswordHasher.verify(password.toCharArray(),
                user.getPasswordSalt(), user.getPasswordHash())) {
            eventBus.publish(AppEvent.of(Events.USER_LOGIN_FAILED)
                    .with("identifier", trimmed)
                    .build());
            audit(user == null ? null : user.getId(), "LOGIN_FAILED", "Wrong password for " + trimmed);
            return OperationResult.fail(ERR_INVALID_CREDENTIALS, "Wrong e-mail / student ID or password.");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            eventBus.publish(AppEvent.of(Events.USER_LOGIN_FAILED)
                    .actor(user.getId())
                    .build());
            return OperationResult.fail(ERR_ACCOUNT_SUSPENDED,
                    "This account is suspended. Please contact the administrator.");
        }
        if (user.getStatus() == UserStatus.PENDING) {
            return OperationResult.fail("ACCOUNT_PENDING", "This account is waiting for approval.");
        }

        Session session = sessionManager.create(user);
        userDao.updateLastLogin(user.getId(), LocalDateTime.now());
        AuthContext.set(session);

        eventBus.publish(AppEvent.of(Events.USER_LOGIN_SUCCESS)
                .actor(user.getId())
                .with("role", user.getRole().name())
                .build());
        audit(user.getId(), "LOGIN", "Signed in");

        return OperationResult.ok(session, "Welcome back, " + user.getFullName() + "!");
    }

    /** Ends the current session. */
    public void logout() {
        String token = AuthContext.currentToken();
        sessionManager.destroy(token);
        eventBus.publish(AppEvent.of(Events.USER_LOGOUT)
                .actor(AuthContext.currentUserId())
                .build());
        AuthContext.clear();
    }

    public OperationResult<User> changePassword(long userId, String oldPassword, String newPassword) {
        User user = userDao.findById(userId).orElse(null);
        if (user == null) {
            return OperationResult.fail(ERR_NOT_FOUND, "User not found.");
        }
        if (!PasswordHasher.verify(oldPassword.toCharArray(),
                user.getPasswordSalt(), user.getPasswordHash())) {
            return OperationResult.fail("WRONG_PASSWORD", "The current password is not correct.");
        }
        if (!Validation.isStrongPassword(newPassword)) {
            return OperationResult.fail(ERR_WEAK_PASSWORD,
                    "New password needs at least 8 characters, a letter and a digit.");
        }
        PasswordHasher.HashedPassword hashed = PasswordHasher.hash(newPassword.toCharArray());
        userDao.updatePassword(userId, hashed.getHash(), hashed.getSalt());
        eventBus.publish(AppEvent.of(Events.PASSWORD_CHANGED).actor(userId).build());
        audit(userId, "PASSWORD_CHANGE", "Password changed");
        return OperationResult.ok(user.sanitized(), "Password updated.");
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
