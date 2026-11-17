package com.bubt.findit.core.model;

import java.time.Instant;

/**
 * A logged-in session. Created by Segment 1 and consumed by everyone else
 * through {@code SessionManager} / {@code AuthContext}.
 */
public class Session {

    private final String token;
    private final User user;
    private final Instant loginAt;
    private final Instant expiresAt;

    public Session(String token, User user, Instant loginAt, Instant expiresAt) {
        this.token = token;
        this.user = user;
        this.loginAt = loginAt;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public Instant getLoginAt() {
        return loginAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isAdmin() {
        return user != null && user.isAdmin();
    }
}
