package com.bubt.findit.core;

import com.bubt.findit.core.model.Session;
import com.bubt.findit.core.model.User;

/**
 * Holds the session of the person currently using the application.
 *
 * <p>This is the single entry point for "who is doing this?" that Segments 2,
 * 3 and 4 use. They never ask the login screen for the user - they ask
 * {@code AuthContext.currentUser()}. That keeps the GUI swappable: if the
 * project later moves to a web front-end, only this class changes.
 */
public final class AuthContext {

    private static final ThreadLocal<Session> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(Session session) {
        CURRENT.set(session);
    }

    public static void clear() {
        CURRENT.remove();
    }

    /** The logged-in user, or null when nobody is logged in. */
    public static User currentUser() {
        Session session = CURRENT.get();
        return session == null ? null : session.getUser();
    }

    /** The logged-in user's id, or null. */
    public static Long currentUserId() {
        User user = currentUser();
        return user == null ? null : user.getId();
    }

    public static String currentToken() {
        Session session = CURRENT.get();
        return session == null ? null : session.getToken();
    }

    public static boolean isLoggedIn() {
        Session session = CURRENT.get();
        return session != null && !session.isExpired();
    }

    public static boolean isAdmin() {
        User user = currentUser();
        return user != null && user.isAdmin();
    }
}
