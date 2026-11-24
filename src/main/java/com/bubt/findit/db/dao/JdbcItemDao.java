package com.bubt.findit.db.dao;

import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.ItemType;
import com.bubt.findit.core.model.LocationCount;
import com.bubt.findit.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * The MySQL implementation of {@link ItemDao}.
 *
 * <p>Every statement is a {@link PreparedStatement} - the values are sent
 * separately from the SQL, so a title containing a quote cannot break the query
 * and cannot inject SQL.
 */
public class JdbcItemDao implements ItemDao {

    @Override
    public long countByType(String itemType) {
        String sql = itemType == null
                ? "SELECT COUNT(*) FROM items"
                : "SELECT COUNT(*) FROM items WHERE item_type = ?";
        return queryCount(sql, itemType);
    }

    @Override
    public long countByCategory(String category) {
        return queryCount("SELECT COUNT(*) FROM items WHERE category = ?", category);
    }

    @Override
    public long countOpen() {
        return queryCount("SELECT COUNT(*) FROM items WHERE status = 'OPEN'", null);
    }

    @Override
    public long countReportedBy(long userId, String itemType) {
        String sql = itemType == null
                ? "SELECT COUNT(*) FROM items WHERE reported_by = ?"
                : "SELECT COUNT(*) FROM items WHERE reported_by = ? AND item_type = ?";
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, userId);
                if (itemType != null) {
                    statement.setString(2, itemType);
                }
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0L;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not count the reported items", ex);
        } finally {
            Database.release(connection);
        }
    }

    @Override
    public List<ItemSummary> recent(int limit) {
        String sql = "SELECT i.item_id, i.title, i.category, i.item_type, i.status, i.created_at, "
                + "u.full_name, COALESCE(l.location_name, i.last_seen_location) AS place "
                + "FROM items i "
                + "JOIN users u ON u.user_id = i.reported_by "
                + "LEFT JOIN locations l ON l.location_id = i.location_id "
                + "ORDER BY i.created_at DESC, i.item_id DESC LIMIT ?";
        List<ItemSummary> items = new ArrayList<>();
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, Math.max(1, limit));
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(new ItemSummary(
                                (long) rs.getInt("item_id"),
                                rs.getString("title"),
                                rs.getString("category"),
                                ItemType.fromString(rs.getString("item_type")),
                                rs.getString("place"),
                                rs.getString("full_name"),
                                null,
                                rs.getString("status"),
                                rs.getTimestamp("created_at") == null
                                        ? null : rs.getTimestamp("created_at").toLocalDateTime()));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not read the recent items", ex);
        } finally {
            Database.release(connection);
        }
        return items;
    }

    @Override
    public List<LocationCount> topLocations(int limit) {
        String sql = "SELECT COALESCE(l.location_name, i.last_seen_location, 'Unknown') AS place, "
                + "COUNT(*) AS total "
                + "FROM items i LEFT JOIN locations l ON l.location_id = i.location_id "
                + "GROUP BY place ORDER BY total DESC, place ASC LIMIT ?";
        List<LocationCount> locations = new ArrayList<>();
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, Math.max(1, limit));
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        locations.add(new LocationCount(rs.getString("place"), rs.getInt("total")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not read the location statistics", ex);
        } finally {
            Database.release(connection);
        }
        return locations;
    }

    @Override
    public long insert(ItemRecord item) {
        String sql = "INSERT INTO items (reported_by, item_type, category, title, description, "
                + "brand, color, last_seen_location, location_id, status, is_urgent, lost_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = null;
        try {
            connection = Database.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, item.getReportedBy());
                statement.setString(2, item.getItemType());
                statement.setString(3, item.getCategory());
                statement.setString(4, item.getTitle());
                statement.setString(5, item.getDescription());
                statement.setString(6, item.getBrand());
                statement.setString(7, item.getColor());
                statement.setString(8, item.getLocationName());
                statement.setObject(9, findLocationId(connection, item.getLocationName()));
                statement.setString(10, item.getStatus());
                statement.setBoolean(11, item.isUrgent());
                statement.setString(12, item.getLostDate());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                }
                return 0L;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not store the item report", ex);
        } finally {
            Database.release(connection);
        }
    }

    /** Looks up (or creates) the location row with this name. */
    private Integer findLocationId(Connection connection, String name) throws SQLException {
        if (name == null || name.isBlank()) {
            return null;
        }
        try (PreparedStatement lookup = connection.prepareStatement(
                "SELECT location_id FROM locations WHERE location_name = ?")) {
            lookup.setString(1, name);
            try (ResultSet rs = lookup.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO locations (location_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, name);
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : null;
            }
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
            throw new DataAccessException("Could not count the items", ex);
        } finally {
            Database.release(connection);
        }
    }
}
