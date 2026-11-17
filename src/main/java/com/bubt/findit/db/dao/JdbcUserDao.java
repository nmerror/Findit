package com.bubt.findit.db.dao;

import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.model.UserStatus;
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
import java.util.Optional;

/** MySQL implementation of {@link UserDao} using plain JDBC. */
public class JdbcUserDao implements UserDao {

    private static final String SELECT_COLUMNS =
            "user_id, full_name, email, student_id, password_hash, password_salt, role, status, phone, created_at, last_login";

    @Override
    public Optional<User> findByEmail(String email) {
        return findOne("SELECT " + SELECT_COLUMNS + " FROM users WHERE email = ?", email);
    }

    @Override
    public Optional<User> findByStudentId(String studentId) {
        return findOne("SELECT " + SELECT_COLUMNS + " FROM users WHERE student_id = ?", studentId);
    }

    @Override
    public Optional<User> findById(long id) {
        return findOne("SELECT " + SELECT_COLUMNS + " FROM users WHERE user_id = ?", id);
    }

    private Optional<User> findOne(String sql, Object value) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not read user row", ex);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public User insert(User user) {
        String sql = "INSERT INTO users (full_name, email, student_id, password_hash, password_salt, "
                + "role, status, phone, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getStudentId());
            statement.setString(4, user.getPasswordHash());
            statement.setString(5, user.getPasswordSalt());
            statement.setString(6, user.getRole().name());
            statement.setString(7, user.getStatus().name());
            statement.setString(8, user.getPhone());
            statement.setTimestamp(9, Timestamp.valueOf(
                    user.getCreatedAt() == null ? LocalDateTime.now() : user.getCreatedAt()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            return user;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not insert user", ex);
        }
    }

    @Override
    public boolean updatePassword(long userId, String passwordHash, String passwordSalt) {
        return update("UPDATE users SET password_hash = ?, password_salt = ? WHERE user_id = ?",
                passwordHash, passwordSalt, userId);
    }

    @Override
    public boolean updateStatus(long userId, UserStatus status) {
        return update("UPDATE users SET status = ? WHERE user_id = ?", status.name(), userId);
    }

    @Override
    public boolean updateRole(long userId, Role role) {
        return update("UPDATE users SET role = ? WHERE user_id = ?", role.name(), userId);
    }

    @Override
    public boolean updateLastLogin(long userId, LocalDateTime loginTime) {
        return update("UPDATE users SET last_login = ? WHERE user_id = ?", Timestamp.valueOf(loginTime), userId);
    }

    private boolean update(String sql, Object... values) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DataAccessException("Could not update user row", ex);
        }
    }

    @Override
    public List<User> findAll(int limit, int offset) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users ORDER BY user_id LIMIT ? OFFSET ?";
        List<User> users = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    users.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not list users", ex);
        }
        return users;
    }

    @Override
    public long countAll() {
        return count("SELECT COUNT(*) FROM users");
    }

    @Override
    public long countByRole(Role role) {
        return count("SELECT COUNT(*) FROM users WHERE role = ?", role.name());
    }

    @Override
    public long countByStatus(UserStatus status) {
        return count("SELECT COUNT(*) FROM users WHERE status = ?", status.name());
    }

    @Override
    public long countReportsByUser(long userId, String itemType) {
        String sql = "SELECT COUNT(*) FROM items WHERE posted_by = ? AND item_type = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, itemType);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not count user reports", ex);
        }
        return 0;
    }

    private long count(String sql, Object... values) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not run count query", ex);
        }
        return 0;
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setStudentId(rs.getString("student_id"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPasswordSalt(rs.getString("password_salt"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setStatus(UserStatus.fromString(rs.getString("status")));
        user.setPhone(rs.getString("phone"));
        Timestamp created = rs.getTimestamp("created_at");
        user.setCreatedAt(created == null ? null : created.toLocalDateTime());
        Timestamp lastLogin = rs.getTimestamp("last_login");
        user.setLastLogin(lastLogin == null ? null : lastLogin.toLocalDateTime());
        return user;
    }
}
