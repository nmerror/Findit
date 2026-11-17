package com.bubt.findit.db.dao;

import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.model.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the {@code users} table. Segment 1 owns this table.
 *
 * <p>Two implementations exist: {@code JdbcUserDao} (MySQL) and
 * {@code InMemoryUserDao} (used in memory mode and by the self-test).
 * Everything above this interface is identical in both modes.
 */
public interface UserDao {

    Optional<User> findByEmail(String email);

    Optional<User> findByStudentId(String studentId);

    Optional<User> findById(long id);

    boolean existsByEmail(String email);

    /** Inserts the user and returns it with the generated id. */
    User insert(User user);

    boolean updatePassword(long userId, String passwordHash, String passwordSalt);

    boolean updateStatus(long userId, UserStatus status);

    boolean updateRole(long userId, Role role);

    boolean updateLastLogin(long userId, LocalDateTime loginTime);

    List<User> findAll(int limit, int offset);

    long countAll();

    long countByRole(Role role);

    long countByStatus(UserStatus status);

    /** Number of items of the given type reported by one user (Segment 2 fills the count). */
    long countReportsByUser(long userId, String itemType);
}
