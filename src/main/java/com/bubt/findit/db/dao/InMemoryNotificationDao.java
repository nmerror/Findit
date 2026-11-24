package com.bubt.findit.db.dao;

import com.bubt.findit.core.model.Notification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The in-memory implementation of {@link NotificationDao} - used when the app
 * runs without a database (demo mode and the self-test).
 */
public class InMemoryNotificationDao implements NotificationDao {

    private final List<Notification> notifications = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public synchronized void insert(Notification notification) {
        notification.setId(nextId.getAndIncrement());
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        notifications.add(notification);
    }

    @Override
    public synchronized List<Notification> recent(long userId, int limit) {
        List<Notification> mine = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.getUserId() != null && notification.getUserId() == userId) {
                mine.add(notification);
            }
        }
        mine.sort(Comparator.comparing(Notification::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return mine.size() > limit ? new ArrayList<>(mine.subList(0, Math.max(0, limit))) : mine;
    }

    @Override
    public synchronized long unreadCount(long userId) {
        return notifications.stream()
                .filter(n -> n.getUserId() != null && n.getUserId() == userId && !n.isRead())
                .count();
    }

    @Override
    public synchronized int markAllRead(long userId) {
        int changed = 0;
        for (Notification notification : notifications) {
            if (notification.getUserId() != null && notification.getUserId() == userId
                    && !notification.isRead()) {
                notification.setRead(true);
                changed++;
            }
        }
        return changed;
    }
}
