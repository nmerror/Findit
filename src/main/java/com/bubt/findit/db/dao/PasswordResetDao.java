package com.bubt.findit.db.dao;

import java.time.LocalDateTime;

/**
 * Data access for the {@code password_resets} table - the PIN codes of the
 * "forgot password" flow.
 *
 * <p>A PIN is short-lived: it is valid for ten minutes and can be used only
 * once. The same rule for every implementation: MySQL and the in-memory mode
 * behave exactly the same, so the screen never depends on the mode.
 */
public interface PasswordResetDao {

    /** Stores a new PIN for the user. */
    boolean create(long userId, String pin, LocalDateTime expiresAt);

    /** True when the PIN belongs to the user and has not expired yet. */
    boolean isValid(long userId, String pin, LocalDateTime now);

    /** Marks the PIN as used, so it cannot be used a second time. */
    void markUsed(long userId, String pin);

    /** Removes the old PINs of a user. */
    void deleteAllOf(long userId);
}
