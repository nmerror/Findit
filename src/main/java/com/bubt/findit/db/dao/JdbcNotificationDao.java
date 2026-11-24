package com.bubt.findit.db.dao;

import com.bubt.findit.core.model.Notification;
import com.bubt.findit.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * The MySQL implementation of {@link NotificationDao}.
 */
public class JdbcNotificationDao implements NotificationDao {

    @Override
    public void insert(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, title, message, is_read) VALUES (?, ?, ?, ?)";
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, notification.getUserId());
                statement.setString(2, notification.getTitle());
                statement.setString(3, notification.getMessage());
                statement.setBoolean(4, notification.isRead());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        notification.setId(keys.getLong(1));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not store the notification", ex);
        } finally {
            Database.release(connection);
        }
    }

    @Override
    public List<Notification> recent(long userId, int limit) {
        String sql = "SELECT notification_id, user_id, title, message, is_read, created_at "
                + "FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        List<Notification> notifications = new ArrayList<>();
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, userId);
                statement.setInt(2, Math.max(1, limit));
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        notifications.add(map(rs));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not read the notifications", ex);
        } finally {
            Database.release(connection);
        }
        return notifications;
    }

    @Override
    public long unreadCount(long userId) {
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0")) {
                statement.setLong(1, userId);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0L;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not count the unread notifications", ex);
        } finally {
            Database.release(connection);
        }
    }

    @Override
    public int markAllRead(long userId) {
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0")) {
                statement.setLong(1, userId);
                return statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not mark the notifications as read", ex);
        } finally {
            Database.release(connection);
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId((long) rs.getInt("notification_id"));
        notification.setUserId((long) rs.getInt("user_id"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getTimestamp("created_at") == null
                ? null : rs.getTimestamp("created_at").toLocalDateTime());
        return notification;
    }
}
