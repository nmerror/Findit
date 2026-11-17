package com.bubt.findit.core.spi.stub;

import com.bubt.findit.core.model.Notification;
import com.bubt.findit.core.spi.NotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Placeholder for the notification feature. It keeps the messages in memory
 * and prints them to the console so the flow is still visible while the real
 * implementation is pending.
 */
public class StubNotificationService implements NotificationService {

    private final List<Notification> inbox = new ArrayList<>();

    @Override
    public void notifyUser(Long userId, String title, String message) {
        Notification notification = new Notification(userId, title, message, false, LocalDateTime.now());
        inbox.add(notification);
        System.out.println("[notification] to user " + userId + " -> " + title + ": " + message);
    }

    @Override
    public long unreadCount(Long userId) {
        return inbox.stream()
                .filter(n -> !n.isRead() && n.getUserId() != null && n.getUserId().equals(userId))
                .count();
    }

    @Override
    public List<Notification> recent(Long userId, int limit) {
        List<Notification> result = new ArrayList<>();
        for (Notification notification : inbox) {
            if (userId == null || userId.equals(notification.getUserId())) {
                result.add(notification);
            }
        }
        Collections.reverse(result);
        return result.size() > limit ? result.subList(0, limit) : result;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
