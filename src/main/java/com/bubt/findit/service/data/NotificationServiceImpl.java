package com.bubt.findit.service.data;

import com.bubt.findit.core.model.Notification;
import com.bubt.findit.core.spi.NotificationService;
import com.bubt.findit.db.dao.NotificationDao;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Stores the in-app notifications in the database.
 *
 * <p>Segment 1 owns the {@code notifications} table, so this implementation is
 * the real one - not a reference. Any segment can call
 * {@code notifyUser(...)} to tell a user something.
 */
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDao notificationDao;

    public NotificationServiceImpl(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Override
    public void notifyUser(Long userId, String title, String message) {
        if (userId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationDao.insert(notification);
    }

    @Override
    public long unreadCount(Long userId) {
        return userId == null ? 0L : notificationDao.unreadCount(userId);
    }

    @Override
    public List<Notification> recent(Long userId, int limit) {
        return userId == null ? List.of() : notificationDao.recent(userId, limit);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
