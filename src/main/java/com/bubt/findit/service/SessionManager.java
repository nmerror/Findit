package com.bubt.findit.service;

import com.bubt.findit.core.model.Session;
import com.bubt.findit.core.model.User;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates, validates and destroys login sessions.
 *
 * <p>Sessions are kept in memory. That is enough for a desktop application
 * where the process lifetime equals the user's session.
 */
public class SessionManager {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Duration timeout;

    public SessionManager(Duration timeout) {
        this.timeout = timeout;
    }

    public Session create(User user) {
        // Remove the sessions that already expired - keeps the map small
        // without a background timer.
        purgeExpired();
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Session session = new Session(token, user, now, now.plus(timeout));
        sessions.put(token, session);
        return session;
    }

    /** Returns the session when the token is known and not expired. */
    public Optional<Session> validate(String token) {
        if (token == null) {
            return Optional.empty();
        }
        Session session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (session.isExpired()) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public void destroy(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public void destroyAll() {
        sessions.clear();
    }

    /** Removes expired sessions - call it periodically from the UI timer. */
    public int purgeExpired() {
        int before = sessions.size();
        sessions.values().removeIf(Session::isExpired);
        return before - sessions.size();
    }

}
