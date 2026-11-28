package com.bubt.findit.db.dao;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The in-memory implementation of {@link PasswordResetDao} - used by the
 * demo mode and by the self-test.
 */
public class InMemoryPasswordResetDao implements PasswordResetDao {

    private record Entry(String pin, LocalDateTime expiresAt, boolean used) {
    }

    private final Map<Long, Map<String, Entry>> codes = new ConcurrentHashMap<>();

    @Override
    public boolean create(long userId, String pin, LocalDateTime expiresAt) {
        codes.computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                .put(pin, new Entry(pin, expiresAt, false));
        return true;
    }

    @Override
    public boolean isValid(long userId, String pin, LocalDateTime now) {
        Entry entry = codes.getOrDefault(userId, Map.of()).get(pin);
        return entry != null && !entry.used() && entry.expiresAt().isAfter(now);
    }

    @Override
    public void markUsed(long userId, String pin) {
        Map<String, Entry> userCodes = codes.get(userId);
        if (userCodes == null) {
            return;
        }
        Entry entry = userCodes.get(pin);
        if (entry != null) {
            userCodes.put(pin, new Entry(entry.pin(), entry.expiresAt(), true));
        }
    }

    @Override
    public void deleteAllOf(long userId) {
        codes.remove(userId);
    }
}
