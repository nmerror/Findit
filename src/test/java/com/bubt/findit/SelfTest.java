package com.bubt.findit;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.config.AppConfig;
import com.bubt.findit.core.AuthContext;
import com.bubt.findit.core.event.AppEvent;
import com.bubt.findit.core.event.Events;
import com.bubt.findit.core.model.DashboardStats;
import com.bubt.findit.core.model.ItemCategory;
import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.ItemType;
import com.bubt.findit.core.model.LocationCount;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.Permission;
import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.Session;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.model.UserStatus;
import com.bubt.findit.core.registry.ServiceRegistry;
import com.bubt.findit.core.spi.ItemCatalogService;
import com.bubt.findit.db.Database;
import com.bubt.findit.db.DaoFactory;
import com.bubt.findit.service.RbacService;
import com.bubt.findit.service.SessionManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Headless self-test for Segment 1. Run it with:
 *
 * <pre>
 *   java -cp target/classes:target/test-classes com.bubt.findit.SelfTest
 * </pre>
 *
 * <p>It checks the three things that matter for the team:
 * <ol>
 *   <li>authentication and RBAC behave correctly,</li>
 *   <li>the event bus delivers Segment 1's events,</li>
 *   <li>the dashboard picks up another segment's implementation through the
 *       service contract - the actual integration test.</li>
 * </ol>
 */
public final class SelfTest {

    private static int passed = 0;
    private static int failed = 0;
    private static final List<String> failures = new ArrayList<>();

    private SelfTest() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.memory();
        Database.initialize(config);
        DaoFactory.reset();
        AppServices services = new AppServices(config);

        testRegistration(services);
        testLogin(services);
        testSuspendedUser(services);
        testRbac();
        testSessionExpiry();
        testEventBus(services);
        testDashboardWithoutSegments(services);
        testDashboardWithSegment2(services);
        testGlobalSearch(services);
        testSearchContributor(services);
        testReport(services);

        System.out.println();
        System.out.println("=========================================");
        System.out.println("  Passed: " + passed + "   Failed: " + failed);
        if (!failures.isEmpty()) {
            System.out.println("  Failing checks:");
            failures.forEach(f -> System.out.println("   - " + f));
        }
        System.out.println("=========================================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------------------------------------------------------------- tests

    private static void testRegistration(AppServices services) {
        OperationResult<User> ok = services.authService()
                .register("Ayesha Rahman", "ayesha@buet.edu", "21-04234", "Passw0rd1", "Passw0rd1");
        check("register valid student", ok.isSuccess());

        OperationResult<User> duplicate = services.authService()
                .register("Someone Else", "ayesha@buet.edu", "21-04235", "Passw0rd1", "Passw0rd1");
        check("reject duplicate e-mail", duplicate.isFailure()
                && "EMAIL_EXISTS".equals(duplicate.getErrorCode()));

        OperationResult<User> weak = services.authService()
                .register("Weak Password", "weak@buet.edu", "21-04236", "short", "short");
        check("reject weak password", weak.isFailure()
                && "WEAK_PASSWORD".equals(weak.getErrorCode()));

        OperationResult<User> mismatch = services.authService()
                .register("Mismatch", "mismatch@buet.edu", "21-04237", "Passw0rd1", "Passw0rd2");
        check("reject password mismatch", mismatch.isFailure()
                && "PASSWORD_MISMATCH".equals(mismatch.getErrorCode()));

        OperationResult<User> badEmail = services.authService()
                .register("Bad Email", "not-an-email", "21-04238", "Passw0rd1", "Passw0rd1");
        check("reject invalid e-mail", badEmail.isFailure()
                && "INVALID_EMAIL".equals(badEmail.getErrorCode()));
    }

    private static void testLogin(AppServices services) {
        OperationResult<Session> ok = services.authService().login("ayesha@buet.edu", "Passw0rd1");
        check("login with correct password", ok.isSuccess());

        OperationResult<Session> byStudentId = services.authService().login("21-04234", "Passw0rd1");
        check("login with student ID", byStudentId.isSuccess());

        OperationResult<Session> wrong = services.authService().login("ayesha@buet.edu", "wrongpass");
        check("reject wrong password", wrong.isFailure()
                && "INVALID_CREDENTIALS".equals(wrong.getErrorCode()));

        OperationResult<Session> unknown = services.authService().login("nobody@buet.edu", "Passw0rd1");
        check("reject unknown user", unknown.isFailure());

        check("password is never stored in plain text", services.userDao()
                .findByEmail("ayesha@buet.edu")
                .map(u -> u.getPasswordHash() != null && !u.getPasswordHash().contains("Passw0rd1"))
                .orElse(false));
    }

    private static void testSuspendedUser(AppServices services) {
        services.authService().register("Bad Actor", "bad@buet.edu", "21-09999", "Passw0rd1", "Passw0rd1");
        User bad = services.userDao().findByEmail("bad@buet.edu").orElse(null);
        check("suspend user", bad != null
                && services.userDao().updateStatus(bad.getId(), UserStatus.SUSPENDED));

        OperationResult<Session> result = services.authService().login("bad@buet.edu", "Passw0rd1");
        check("suspended user cannot log in", result.isFailure()
                && "ACCOUNT_SUSPENDED".equals(result.getErrorCode()));
    }

    private static void testRbac() {
        RbacService rbac = new RbacService();
        User student = new User();
        student.setRole(Role.STUDENT);
        student.setStatus(UserStatus.ACTIVE);

        User admin = new User();
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        check("student may report an item", rbac.can(student, Permission.REPORT_ITEM));
        check("student may submit a claim", rbac.can(student, Permission.SUBMIT_CLAIM));
        check("student may NOT approve a claim", !rbac.can(student, Permission.APPROVE_CLAIM));
        check("student may NOT manage users", !rbac.can(student, Permission.MANAGE_USERS));
        check("admin may approve a claim", rbac.can(admin, Permission.APPROVE_CLAIM));
        check("admin may manage users", rbac.can(admin, Permission.MANAGE_USERS));

        boolean denied = false;
        try {
            rbac.require(student, Permission.APPROVE_CLAIM);
        } catch (com.bubt.findit.service.AccessDeniedException ex) {
            denied = true;
        }
        check("require() throws AccessDeniedException", denied);
    }

    private static void testSessionExpiry() {
        SessionManager manager = new SessionManager(Duration.ofMillis(1));
        User user = new User();
        user.setId(1L);
        user.setRole(Role.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        Session session = manager.create(user);
        sleep(20);
        check("expired session is rejected", manager.validate(session.getToken()).isEmpty());
    }

    private static void testEventBus(AppServices services) {
        AtomicInteger received = new AtomicInteger();
        services.eventBus().subscribe(Events.USER_REGISTERED, event -> received.incrementAndGet());
        services.authService().register("Event Tester", "event@buet.edu", "21-08888", "Passw0rd1", "Passw0rd1");
        check("USER_REGISTERED event delivered", received.get() == 1);

        AtomicInteger loginEvents = new AtomicInteger();
        services.eventBus().subscribe(Events.USER_LOGIN_SUCCESS, event -> loginEvents.incrementAndGet());
        services.authService().login("event@buet.edu", "Passw0rd1");
        check("USER_LOGIN_SUCCESS event delivered", loginEvents.get() == 1);

        boolean survived = true;
        try {
            services.eventBus().subscribe(Events.USER_LOGOUT, event -> {
                throw new IllegalStateException("boom");
            });
            services.eventBus().publish(AppEvent.of(Events.USER_LOGOUT).build());
        } catch (RuntimeException ex) {
            survived = false;
        }
        check("a failing listener does not break the publisher", survived);
    }

    private static void testDashboardWithoutSegments(AppServices services) {
        DashboardStats stats = services.dashboardService().build(null);
        check("dashboard builds without the other segments", stats != null);
        check("dashboard reports the missing segments",
                !services.dashboardService().missingSegments().isEmpty());
        check("user counter comes from Segment 1 data", stats.getTotalUsers() >= 3);
    }

    /**
     * THE INTEGRATION TEST: a fake Segment 2 registers itself and the
     * dashboard immediately shows its numbers - no change in Segment 1 code.
     */
    private static void testDashboardWithSegment2(AppServices services) {
        ServiceRegistry.register(ItemCatalogService.class, new FakeSegment2Catalog());

        DashboardStats stats = services.dashboardService().build(null);
        check("dashboard picks up Segment 2 through the contract", stats.getTotalLostItems() == 7);
        check("dashboard picks up the location chart", stats.getTopLocations().size() == 1);
        check("Segment 2 is no longer listed as missing",
                !services.dashboardService().missingSegments().contains("Segment 2 - Item Reporting"));

        ServiceRegistry.register(ItemCatalogService.class,
                new com.bubt.findit.core.spi.stub.StubItemCatalogService());
    }

    // -------------------------------------------------------------- search

    private static void testGlobalSearch(AppServices services) {
        com.bubt.findit.service.SearchService search = services.searchService();

        check("search finds a user by name",
                search.search("Ayesha", 20).stream().anyMatch(h -> h.getKind().equals("PERSON")));
        check("search finds a user by e-mail",
                search.search("event@buet.edu", 20).stream().anyMatch(h -> h.getKind().equals("PERSON")));
        check("search finds a user by student ID",
                search.search("21-04234", 20).stream().anyMatch(h -> h.getKind().equals("PERSON")));
        check("search with a blank query returns nothing", search.search("  ", 20).isEmpty());
        check("search for nonsense returns nothing", search.search("zzzznotfound", 20).isEmpty());
    }

    /**
     * THE PLUG-IN TEST: another segment registers a search contributor and the
     * global search immediately returns its rows - without any change in
     * Segment 1's code.
     */
    private static void testSearchContributor(AppServices services) {
        ServiceRegistry.registerSearchContributor(new FakeSearchContributor());
        List<com.bubt.findit.core.model.SearchHit> hits =
                services.searchService().search("umbrella", 20);
        check("a registered contributor joins the global search",
                hits.stream().anyMatch(h -> h.getSource().equals("ITEMS")));
        check("the search still finds people after the contributor registered",
                services.searchService().search("Ayesha", 20).stream()
                        .anyMatch(h -> h.getKind().equals("PERSON")));
        ServiceRegistry.reset();
        ServiceRegistry.registerDefaults();
    }

    // -------------------------------------------------------------- report

    private static void testReport(AppServices services) {
        com.bubt.findit.service.ReportService report = services.reportService();
        com.bubt.findit.core.model.DashboardStats stats = services.dashboardService().build(null);

        String text = report.buildTextReport(null, stats, List.of());
        check("the report has the accounts section", text.contains("ACCOUNTS"));
        check("the report has the items section", text.contains("LOST & FOUND ITEMS"));
        check("the report has the claim section", text.contains("CLAIM PIPELINE"));
        check("the report has the audit section", text.contains("RECENT ACTIVITY"));
        check("the report shows the number of users", text.contains(String.valueOf(stats.getTotalUsers())));

        String csv = report.buildCsvReport(null, stats);
        check("the CSV report starts with a header row", csv.startsWith("section,metric,value"));
        check("the CSV report has one row per metric",
                csv.lines().filter(line -> line.startsWith("accounts,")).count() == 5);
    }

    // ------------------------------------------------------------- helpers

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

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /** Stands in for a Segment 2/3/4 search contributor. */
    private static final class FakeSearchContributor
            implements com.bubt.findit.core.spi.SearchContributor {

        @Override
        public List<com.bubt.findit.core.model.SearchHit> search(String query, int limit) {
            if (!query.contains("umbrella")) {
                return List.of();
            }
            return List.of(new com.bubt.findit.core.model.SearchHit(
                    "ITEMS", "Blue umbrella", "Found near the cafeteria", "ITEM", 99L));
        }

        @Override
        public String sourceName() {
            return "ITEMS";
        }
    }

    /** Stands in for the real Segment 2 implementation. */
    private static final class FakeSegment2Catalog implements ItemCatalogService {

        @Override
        public List<ItemSummary> recentItems(int limit) {
            List<ItemSummary> items = new ArrayList<>();
            items.add(new ItemSummary(1L, "Blue Wallet", "ACCESSORIES", ItemType.LOST,
                    "Library", "Ayesha Rahman", 1L, "OPEN", LocalDateTime.now()));
            return items;
        }

        @Override
        public int countByType(ItemType type) {
            return type == ItemType.LOST ? 7 : 3;
        }

        @Override
        public int countByCategory(ItemCategory category) {
            return 2;
        }

        @Override
        public List<LocationCount> topLocations(int limit) {
            return List.of(new LocationCount("Library", 5));
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}
