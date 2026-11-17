package com.bubt.findit.db.dao;

import com.bubt.findit.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** MySQL implementation of {@link AuditDao}. */
public class JdbcAuditDao implements AuditDao {

    @Override
    public void insert(AuditEntry entry) {
        String sql = "INSERT INTO audit_log (actor_user_id, action, details, created_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (entry.getActorUserId() == null) {
                statement.setNull(1, java.sql.Types.BIGINT);
            } else {
                statement.setLong(1, entry.getActorUserId());
            }
            statement.setString(2, entry.getAction());
            statement.setString(3, entry.getDetails());
            statement.setTimestamp(4, Timestamp.valueOf(
                    entry.getCreatedAt() == null ? LocalDateTime.now() : entry.getCreatedAt()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entry.setId(keys.getLong(1));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not write audit entry", ex);
        }
    }

    @Override
    public List<AuditEntry> recent(int limit) {
        return query("SELECT audit_id, actor_user_id, action, details, created_at "
                + "FROM audit_log ORDER BY audit_id DESC LIMIT ?", limit);
    }

    @Override
    public List<AuditEntry> byActor(long actorUserId, int limit) {
        String sql = "SELECT audit_id, actor_user_id, action, details, created_at "
                + "FROM audit_log WHERE actor_user_id = ? ORDER BY audit_id DESC LIMIT ?";
        List<AuditEntry> entries = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, actorUserId);
            statement.setInt(2, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    entries.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not read audit log", ex);
        }
        return entries;
    }

    private List<AuditEntry> query(String sql, int limit) {
        List<AuditEntry> entries = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    entries.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not read audit log", ex);
        }
        return entries;
    }

    @Override
    public int deleteOlderThan(LocalDateTime cutoff) {
        String sql = "DELETE FROM audit_log WHERE created_at < ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(cutoff));
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Could not delete old audit entries", ex);
        }
    }

    private AuditEntry map(ResultSet rs) throws SQLException {
        AuditEntry entry = new AuditEntry();
        entry.setId(rs.getLong("audit_id"));
        long actor = rs.getLong("actor_user_id");
        entry.setActorUserId(rs.wasNull() ? null : actor);
        entry.setAction(rs.getString("action"));
        entry.setDetails(rs.getString("details"));
        Timestamp created = rs.getTimestamp("created_at");
        entry.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return entry;
    }
}
