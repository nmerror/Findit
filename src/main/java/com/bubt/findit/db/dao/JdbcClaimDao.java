package com.bubt.findit.db.dao;

import com.bubt.findit.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The MySQL implementation of {@link ClaimDao}.
 */
public class JdbcClaimDao implements ClaimDao {

    @Override
    public long countByStatus(String status) {
        return queryCount("SELECT COUNT(*) FROM claims WHERE status = ?", status);
    }

    @Override
    public long countByUser(long userId) {
        return queryCountById("SELECT COUNT(*) FROM claims WHERE claimant_id = ?", userId);
    }

    @Override
    public long countByItem(long itemId) {
        return queryCountById("SELECT COUNT(*) FROM claims WHERE item_id = ?", itemId);
    }

    @Override
    public long insert(ClaimRecord claim) {
        String sql = "INSERT INTO claims (item_id, claimant_id, match_id, description_of_item, "
                + "proof_details, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, claim.getItemId());
                statement.setLong(2, claim.getClaimantId());
                if (claim.getMatchId() == null) {
                    statement.setNull(3, java.sql.Types.INTEGER);
                } else {
                    statement.setLong(3, claim.getMatchId());
                }
                statement.setString(4, claim.getDescriptionOfItem());
                statement.setString(5, claim.getProofDetails());
                statement.setString(6, claim.getStatus());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                }
                return 0L;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not store the claim", ex);
        } finally {
            Database.release(connection);
        }
    }

    @Override
    public boolean updateStatus(long claimId, String status, Long verifiedBy, String decisionNote) {
        String sql = "UPDATE claims SET status = ?, verified_by = ?, decision_note = ?, "
                + "decided_at = CURRENT_TIMESTAMP WHERE claim_id = ?";
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, status);
                if (verifiedBy == null) {
                    statement.setNull(2, java.sql.Types.INTEGER);
                } else {
                    statement.setLong(2, verifiedBy);
                }
                statement.setString(3, decisionNote);
                statement.setLong(4, claimId);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not update the claim", ex);
        } finally {
            Database.release(connection);
        }
    }

    private long queryCountById(String sql, long parameter) {
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, parameter);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0L;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not count the claims", ex);
        } finally {
            Database.release(connection);
        }
    }

    private long queryCount(String sql, String parameter) {
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, parameter);
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0L;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not count the claims", ex);
        } finally {
            Database.release(connection);
        }
    }
}
