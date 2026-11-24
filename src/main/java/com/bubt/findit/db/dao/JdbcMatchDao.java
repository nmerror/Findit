package com.bubt.findit.db.dao;

import com.bubt.findit.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The MySQL implementation of {@link MatchDao}.
 */
public class JdbcMatchDao implements MatchDao {

    @Override
    public long countByStatus(String status) {
        return queryCount("SELECT COUNT(*) FROM matches WHERE status = ?", status);
    }

    @Override
    public long countAll() {
        return queryCount("SELECT COUNT(*) FROM matches", null);
    }

    @Override
    public long insert(MatchRecord match) {
        String sql = "INSERT INTO matches (lost_item_id, found_item_id, score, status, algorithm) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, match.getLostItemId());
                statement.setLong(2, match.getFoundItemId());
                statement.setDouble(3, match.getScore());
                statement.setString(4, match.getStatus());
                statement.setString(5, match.getAlgorithm());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                }
                return 0L;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not store the match", ex);
        } finally {
            Database.release(connection);
        }
    }

    private long queryCount(String sql, String parameter) {
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (parameter != null) {
                    statement.setString(1, parameter);
                }
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0L;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not count the matches", ex);
        } finally {
            Database.release(connection);
        }
    }
}
