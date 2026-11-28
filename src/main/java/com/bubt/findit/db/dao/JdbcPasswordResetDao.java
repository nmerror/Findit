package com.bubt.findit.db.dao;

import com.bubt.findit.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/** The MySQL implementation of {@link PasswordResetDao}. */
public class JdbcPasswordResetDao implements PasswordResetDao {

    @Override
    public boolean create(long userId, String pin, LocalDateTime expiresAt) {
        String sql = "INSERT INTO password_resets (user_id, pin_code, expires_at) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, pin);
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(expiresAt));
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not store the reset code", ex);
        }
    }

    @Override
    public boolean isValid(long userId, String pin, LocalDateTime now) {
        String sql = "SELECT 1 FROM password_resets WHERE user_id = ? AND pin_code = ? "
                + "AND used_at IS NULL AND expires_at > ? LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, pin);
            statement.setTimestamp(3, java.sql.Timestamp.valueOf(now));
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not check the reset code", ex);
        }
    }

    @Override
    public void markUsed(long userId, String pin) {
        String sql = "UPDATE password_resets SET used_at = ? WHERE user_id = ? AND pin_code = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(2, userId);
            statement.setString(3, pin);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not close the reset code", ex);
        }
    }

    @Override
    public void deleteAllOf(long userId) {
        String sql = "DELETE FROM password_resets WHERE user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not clear the reset codes", ex);
        }
    }
}
