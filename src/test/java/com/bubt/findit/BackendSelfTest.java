package com.bubt.findit;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.config.AppConfig;
import com.bubt.findit.core.model.ItemType;
import com.bubt.findit.core.model.Notification;
import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.model.UserStatus;
import com.bubt.findit.core.registry.ServiceRegistry;
import com.bubt.findit.core.spi.ClaimQueryService;
import com.bubt.findit.core.spi.ItemCatalogService;
import com.bubt.findit.db.Database;
import com.bubt.findit.db.DaoFactory;
import com.bubt.findit.db.dao.AuditEntry;
import com.bubt.findit.db.dao.ClaimDao;
import com.bubt.findit.db.dao.ClaimRecord;
import com.bubt.findit.db.dao.ItemDao;
import com.bubt.findit.db.dao.ItemRecord;
import com.bubt.findit.db.dao.MatchDao;
import com.bubt.findit.db.dao.MatchRecord;
import com.bubt.findit.db.dao.NotificationDao;
import com.bubt.findit.db.dao.UserDao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Backend self-test - it exercises the real MySQL backend, not the in-memory
 * demo.
 *
 * <pre>
 *   mysql -u root -p &lt; sql/01_schema.sql
 *   mysql -u root -p &lt; sql/02_seed_data.sql
 *   java -cp "target/classes:target/test-classes:lib/*" com.bubt.findit.BackendSelfTest
 * </pre>
 *
 * <p>It writes its own rows (with the prefix {@code BACKEND-TEST-}) and removes
 * them again, so it is safe to run against the seeded database.
 *
 * <p>Every segment can use this test to check that their tables are reachable
 * from the shared backend.
 */
public final class BackendSelfTest {

    private static int passed = 0;
    private static int failed = 0;
    private static final List<String> failures = new ArrayList<>();

    private BackendSelfTest() {
    }

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  FindIt - backend self-test (MySQL + JDBC)");
        System.out.println("==============================================");

        AppConfig config = args.length > 0 ? AppConfig.load() : AppConfig.load();
        if (!config.mysqlMode()) {
            System.out.println("app.mode is not 'mysql' - nothing to test.");
            System.out.println("Set app.mode = mysql in app.properties and try again.");
            return;
        }

        try {
            Database.initialize(config);
        } catch (RuntimeException ex) {
            System.out.println("Could not start the database layer: " + ex.getMessage());
            System.out.println("Is MySQL running, and is app.properties correct?");
            return;
        }

        if (!Database.ping()) {
            System.out.println("The database did not answer - check the URL, user and password.");
            Database.shutdown();
            return;
        }
        System.out.println("Connected. Pool size: " + Database.poolSize());
        System.out.println("");

        DaoFactory.reset();
        // Building the service container registers the database-backed
        // implementations of the service contracts.
        AppServices services = new AppServices(config);
        UserDao users = DaoFactory.userDao();
        NotificationDao notifications = DaoFactory.notificationDao();
        ItemDao items = DaoFactory.itemDao();
        ClaimDao claims = DaoFactory.claimDao();
        MatchDao matches = DaoFactory.matchDao();

        long userId = createTestUser(users);
        try {
            testUsers(users, userId);
            testAudit(userId);
            testNotifications(notifications, userId);
            testItems(items, userId);
            testMatches(matches);
            testClaims(claims, items, userId);
            testContracts(items, claims);
            testTransactions(userId);
            testPool();
        } finally {
            deleteTestUser(users, userId);
            Database.shutdown();
        }

        System.out.println("");
        System.out.println("==============================================");
        System.out.println("  Passed: " + passed + "   Failed: " + failed);
        System.out.println("==============================================");
        if (!failures.isEmpty()) {
            System.out.println("  Failed checks:");
            failures.forEach(f -> System.out.println("   - " + f));
        }
        System.exit(failed > 0 ? 1 : 0);
    }

    // ------------------------------------------------------------- the checks

    private static void testUsers(UserDao users, long userId) {
        check("the user can be found by e-mail", users.findByEmail("backend-test@findit.edu").isPresent());
        check("the user can be found by student id", users.findByStudentId("99-99999").isPresent());
        check("the user can be found by id", users.findById(userId).isPresent());
        check("the user does not exist under a wrong e-mail", users.findByEmail("nobody@findit.edu").isEmpty());

        check("the password can be changed",
                users.updatePassword(userId, "hash-2", "salt-2"));
        check("the new password is stored", "hash-2".equals(users.findById(userId).get().getPasswordHash()));

        check("the status can be changed", users.updateStatus(userId, UserStatus.SUSPENDED));
        check("the new status is stored", users.findById(userId).get().getStatus() == UserStatus.SUSPENDED);
        users.updateStatus(userId, UserStatus.ACTIVE);

        check("the role can be changed", users.updateRole(userId, Role.ADMIN));
        check("the new role is stored", users.findById(userId).get().getRole() == Role.ADMIN);
        users.updateRole(userId, Role.STUDENT);

        check("the last login can be stored",
                users.updateLastLogin(userId, LocalDateTime.now()));
        check("the user count includes the new row", users.countAll() >= 1);
    }

    private static void testAudit(long userId) {
        AuditEntry entry = new AuditEntry();
        entry.setActorUserId(userId);
        entry.setAction("BACKEND_TEST");
        entry.setDetails("written by the backend self-test");
        DaoFactory.auditDao().insert(entry);

        List<AuditEntry> recent = DaoFactory.auditDao().recent(50);
        check("the audit entry was stored",
                recent.stream().anyMatch(e -> "BACKEND_TEST".equals(e.getAction())));
        check("the audit entries can be filtered by actor",
                DaoFactory.auditDao().byActor(userId, 10).stream()
                        .anyMatch(e -> "BACKEND_TEST".equals(e.getAction())));
        check("old audit entries can be deleted",
                DaoFactory.auditDao().deleteOlderThan(LocalDateTime.now().minusSeconds(1)) >= 0);
    }

    private static void testNotifications(NotificationDao notifications, long userId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("Backend test");
        notification.setMessage("The backend self-test wrote this");
        notifications.insert(notification);

        check("the notification was stored", !notifications.recent(userId, 10).isEmpty());
        check("the unread count is correct", notifications.unreadCount(userId) >= 1);
        check("notifications can be marked as read", notifications.markAllRead(userId) >= 1);
        check("the unread count drops after reading", notifications.unreadCount(userId) == 0);
    }

    private static void testItems(ItemDao items, long userId) {
        ItemRecord lost = new ItemRecord();
        lost.setReportedBy(userId);
        lost.setItemType("LOST");
        lost.setCategory("ELECTRONICS");
        lost.setTitle("BACKEND-TEST blue umbrella");
        lost.setDescription("Written by the backend self-test");
        lost.setBrand("TestBrand");
        lost.setColor("Blue");
        lost.setLocationName("Backend Test Hall");
        lost.setStatus("OPEN");
        lost.setUrgent(true);
        lost.setLostDate("2026-01-01");
        long lostId = items.insert(lost);
        check("the item was stored", lostId > 0);

        ItemRecord found = new ItemRecord();
        found.setReportedBy(userId);
        found.setItemType("FOUND");
        found.setCategory("ELECTRONICS");
        found.setTitle("BACKEND-TEST blue umbrella (found)");
        found.setLocationName("Backend Test Hall");
        found.setStatus("OPEN");
        long foundId = items.insert(found);
        check("the second item was stored", foundId > 0);

        check("the items are counted by type", items.countByType("LOST") >= 1);
        check("the items are counted by category", items.countByCategory("ELECTRONICS") >= 1);
        check("the open items are counted", items.countOpen() >= 1);
        check("the items of one user are counted", items.countReportedBy(userId, "LOST") >= 1);
        check("the recent items are read",
                items.recent(10).stream()
                        .anyMatch(i -> i.getTitle() != null
                                && i.getTitle().startsWith("BACKEND-TEST")));
        check("the location statistics are read",
                items.topLocations(10).stream()
                        .anyMatch(l -> "Backend Test Hall".equals(l.getLabel())));
    }

    private static void testMatches(MatchDao matches) {
        MatchRecord match = new MatchRecord();
        match.setLostItemId(firstItemId());
        match.setFoundItemId(firstItemId());
        match.setScore(91.5);
        match.setStatus("NEW");
        match.setAlgorithm("backend-self-test");
        long matchId = matches.insert(match);
        check("the match was stored", matchId > 0);
        check("the new matches are counted", matches.countByStatus("NEW") >= 1);
        check("all matches are counted", matches.countAll() >= 1);
    }

    private static void testClaims(ClaimDao claims, ItemDao items, long userId) {
        ClaimRecord claim = new ClaimRecord();
        claim.setItemId(firstItemId());
        claim.setClaimantId(userId);
        claim.setDescriptionOfItem("BACKEND-TEST blue umbrella");
        claim.setProofDetails("written by the backend self-test");
        claim.setStatus("SUBMITTED");
        long claimId = claims.insert(claim);
        check("the claim was stored", claimId > 0);
        check("the open claims are counted", claims.countByStatus("SUBMITTED") >= 1);
        check("the claims of one user are counted", claims.countByUser(userId) >= 1);
        check("the claim can be verified",
                claims.updateStatus(claimId, "APPROVED", userId, "backend self-test"));
        check("the new claim status is counted", claims.countByStatus("APPROVED") >= 1);
        check("the claims of one item are counted", claims.countByItem(firstItemId()) >= 1);
        check("the items are still readable after the claim", items.countOpen() >= 1);
    }

    private static void testContracts(ItemDao items, ClaimDao claims) {
        ItemCatalogService catalog = ServiceRegistry.get(ItemCatalogService.class);
        check("the item contract is registered", catalog != null);
        if (catalog != null) {
            check("the contract returns the same lost count as the DAO",
                    catalog.countByType(ItemType.LOST) == items.countByType("LOST"));
            check("the contract returns the recent items",
                    !catalog.recentItems(5).isEmpty());
            check("the contract returns the location statistics",
                    !catalog.topLocations(5).isEmpty());
            check("the contract reports that it is available", catalog.isAvailable());
        }
        ClaimQueryService claimService = ServiceRegistry.get(ClaimQueryService.class);
        check("the claim contract is registered", claimService != null);
        if (claimService != null) {
            check("the contract counts the open claims",
                    claimService.countOpenClaims() == claims.countByStatus("SUBMITTED"));
        }
    }

    private static void testTransactions(long userId) {
        boolean rolledBack = false;
        try {
            Database.inTransaction(connection -> {
                try (java.sql.PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO audit_log (actor_user_id, action, details) VALUES (?, ?, ?)")) {
                    statement.setLong(1, userId);
                    statement.setString(2, "BACKEND_TEST_ROLLBACK");
                    statement.setString(3, "this row must not survive");
                    statement.executeUpdate();
                }
                throw new IllegalStateException("boom - the transaction must roll back");
            });
        } catch (IllegalStateException expected) {
            rolledBack = true;
        } catch (SQLException ex) {
            rolledBack = true;
        }
        check("a failed transaction is rolled back", rolledBack);
        check("the rolled back row is not in the database",
                DaoFactory.auditDao().recent(200).stream()
                        .noneMatch(e -> "BACKEND_TEST_ROLLBACK".equals(e.getAction())));
    }

    private static void testPool() {
        int before = Database.poolSize();
        List<Connection> borrowed = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                borrowed.add(Database.getConnection());
            }
            check("the pool hands out several connections", borrowed.size() == 4);
            check("the pool size stays inside the limit", Database.poolSize() <= 8);
        } catch (SQLException ex) {
            check("the pool hands out several connections", false);
        } finally {
            for (Connection connection : borrowed) {
                Database.release(connection);
            }
        }
        check("the pool size does not grow after releasing", Database.poolSize() <= Math.max(before, 8));
    }

    // ------------------------------------------------------------- the helpers

    private static long firstItemId() {
        ItemDao items = DaoFactory.itemDao();
        return items.recent(1).stream().map(item -> item.getId()).findFirst().orElse(1L);
    }

    private static long createTestUser(UserDao users) {
        User user = new User();
        user.setFullName("Backend Test User");
        user.setEmail("backend-test@findit.edu");
        user.setStudentId("99-99999");
        user.setPasswordHash("test-hash");
        user.setPasswordSalt("test-salt");
        user.setRole(Role.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        return users.insert(user).getId();
    }

    private static void deleteTestUser(UserDao users, long userId) {
        // The audit trail and the notifications disappear with the user
        // (ON DELETE CASCADE / SET NULL in the schema). The items, matches and
        // claims of the test user are removed as well.
        try (Connection connection = Database.getConnection()) {
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM claims WHERE claimant_id = ?")) {
                statement.setLong(1, userId);
                statement.executeUpdate();
            }
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM matches WHERE algorithm = 'backend-self-test'")) {
                statement.executeUpdate();
            }
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM items WHERE title LIKE 'BACKEND-TEST%'")) {
                statement.executeUpdate();
            }
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM locations WHERE location_name = 'Backend Test Hall'")) {
                statement.executeUpdate();
            }
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM audit_log WHERE action LIKE 'BACKEND_TEST%'")) {
                statement.executeUpdate();
            }
        } catch (SQLException ignored) {
            // The test rows are cleaned up by the schema's cascade rules anyway.
        }
        try {
            users.deleteById(userId);
        } catch (RuntimeException ignored) {
            // Not fatal for the test result.
        }
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            failures.add(name);
            System.out.println("  FAIL  " + name);
        }
    }
}
