package com.bubt.findit.db.dao;

import com.bubt.findit.core.model.Notification;

import java.util.List;

/**
 * Stores the in-app notifications of a user.
 *
 * <p>Segment 1 owns the {@code notifications} table - it is the place where
 * "your claim was approved" is written.
 */
public interface NotificationDao {

    /** Stores a notification. */
    void insert(Notification notification);

    /** The newest notifications of one user. */
    List<Notification> recent(long userId, int limit);

    /** How many notifications the user has not read yet. */
    long unreadCount(long userId);

    /** Marks every notification of the user as read. Returns how many changed. */
    int markAllRead(long userId);
}
