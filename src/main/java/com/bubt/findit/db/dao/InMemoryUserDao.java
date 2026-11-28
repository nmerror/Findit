package com.bubt.findit.db.dao;

import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.model.UserStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of {@link UserDao}.
 *
 * <p>Used when {@code app.mode=memory}. It lets the whole application (and the
 * automated self-test) run without a MySQL server.
 */
public class InMemoryUserDao implements UserDao {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return users.values().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }

    @Override
    public Optional<User> findByStudentId(String studentId) {
        if (studentId == null) {
            return Optional.empty();
        }
        return users.values().stream()
                .filter(u -> studentId.equalsIgnoreCase(u.getStudentId()))
                .findFirst();
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public User insert(User user) {
        user.setId(sequence.incrementAndGet());
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean updatePassword(long userId, String passwordHash, String passwordSalt) {
        User user = users.get(userId);
        if (user == null) {
            return false;
        }
        user.setPasswordHash(passwordHash);
        user.setPasswordSalt(passwordSalt);
        return true;
    }

    @Override
    public boolean updateStatus(long userId, UserStatus status) {
        User user = users.get(userId);
        if (user == null) {
            return false;
        }
        user.setStatus(status);
        return true;
    }

    @Override
    public boolean updateRole(long userId, Role role) {
        User user = users.get(userId);
        if (user == null) {
            return false;
        }
        user.setRole(role);
        return true;
    }

    @Override
    public boolean updateLastLogin(long userId, LocalDateTime loginTime) {
        User user = users.get(userId);
        if (user == null) {
            return false;
        }
        user.setLastLogin(loginTime);
        return true;
    }

    @Override
    public List<User> findAll(int limit, int offset) {
        List<User> all = new ArrayList<>(users.values());
        all.sort(Comparator.comparing(User::getId));
        int from = Math.min(offset, all.size());
        int to = Math.min(from + limit, all.size());
        return new ArrayList<>(all.subList(from, to));
    }

    @Override
    public List<User> findByRole(Role role, int limit) {
        List<User> matching = new ArrayList<>();
        for (User user : users.values()) {
            if (role == null || user.getRole() == role) {
                matching.add(user);
            }
        }
        matching.sort(Comparator.comparing(User::getId));
        return new ArrayList<>(matching.subList(0, Math.min(limit, matching.size())));
    }

    @Override
    public long countAll() {
        return users.size();
    }

    @Override
    public long countByRole(Role role) {
        return users.values().stream().filter(u -> u.getRole() == role).count();
    }

    @Override
    public long countByStatus(UserStatus status) {
        return users.values().stream().filter(u -> u.getStatus() == status).count();
    }

    @Override
    public long countReportsByUser(long userId, String itemType) {
        // Segment 2 owns the items table; in memory mode we have no item data.
        return 0;
    }

    @Override
    public boolean deleteById(long userId) {
        return users.remove(userId) != null;
    }
}
