package com.bubt.findit.core.spi;

import com.bubt.findit.core.model.Notification;

import java.util.List;

/**
 * ============================================================================
 *  SERVICE CONTRACT - implemented by SEGMENT 4 (or the shared notification
 *  module). Segment 1 calls this whenever something happens that the user
 *  should know about - "your claim was approved", "an admin suspended you".
 * ============================================================================
 */
public interface NotificationService {

    void notifyUser(Long userId, String title, String message);

    long unreadCount(Long userId);

    List<Notification> recent(Long userId, int limit);

    default boolean isAvailable() {
        return true;
    }
}
