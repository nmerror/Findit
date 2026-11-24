package com.bubt.findit.app;

import com.bubt.findit.config.AppConfig;
import com.bubt.findit.core.event.EventBus;
import com.bubt.findit.core.registry.ServiceRegistry;
import com.bubt.findit.core.spi.AuditService;
import com.bubt.findit.core.spi.AdminWorkloadService;
import com.bubt.findit.core.spi.ClaimQueryService;
import com.bubt.findit.core.spi.ItemCatalogService;
import com.bubt.findit.core.spi.MatchQueryService;
import com.bubt.findit.core.spi.NotificationService;
import com.bubt.findit.db.DaoFactory;
import com.bubt.findit.db.dao.AuditDao;
import com.bubt.findit.db.dao.ClaimDao;
import com.bubt.findit.db.dao.ItemDao;
import com.bubt.findit.db.dao.MatchDao;
import com.bubt.findit.db.dao.NotificationDao;
import com.bubt.findit.db.dao.UserDao;
import com.bubt.findit.service.AuditServiceImpl;
import com.bubt.findit.service.AuthService;
import com.bubt.findit.service.DashboardService;
import com.bubt.findit.service.RbacService;
import com.bubt.findit.service.ReportService;
import com.bubt.findit.service.SearchService;
import com.bubt.findit.service.SessionManager;
import com.bubt.findit.service.UserService;
import com.bubt.findit.service.data.AdminWorkloadServiceImpl;
import com.bubt.findit.service.data.ClaimQueryServiceImpl;
import com.bubt.findit.service.data.ItemCatalogServiceImpl;
import com.bubt.findit.service.data.MatchQueryServiceImpl;
import com.bubt.findit.service.data.NotificationServiceImpl;

import java.time.Duration;

/**
 * The application container: builds every service once and hands them to the
 * screens.
 *
 * <p>This is the single place where the wiring happens. If Segment 2 finishes
 * first, the only change in this whole file is one extra
 * {@code ServiceRegistry.register(...)} line - the screens do not change.
 */
public class AppServices {

    private final AppConfig config;
    private final EventBus eventBus = new EventBus();
    private final UserDao userDao;
    private final AuditDao auditDao;
    private final NotificationDao notificationDao;
    private final ItemDao itemDao;
    private final ClaimDao claimDao;
    private final MatchDao matchDao;
    private final SessionManager sessionManager;
    private final AuthService authService;
    private final RbacService rbacService;
    private final UserService userService;
    private final DashboardService dashboardService;
    private final SearchService searchService;
    private final ReportService reportService;

    public AppServices(AppConfig config) {
        this.config = config;
        this.userDao = DaoFactory.userDao();
        this.auditDao = DaoFactory.auditDao();
        this.notificationDao = DaoFactory.notificationDao();
        this.itemDao = DaoFactory.itemDao();
        this.claimDao = DaoFactory.claimDao();
        this.matchDao = DaoFactory.matchDao();
        this.sessionManager = new SessionManager(Duration.ofMinutes(config.sessionTimeoutMinutes()));
        this.rbacService = new RbacService();
        this.authService = new AuthService(userDao, sessionManager, eventBus);
        this.userService = new UserService(userDao, rbacService, eventBus, auditDao);
        this.dashboardService = new DashboardService(userDao);
        this.searchService = new SearchService(userDao);
        this.reportService = new ReportService(
                ServiceRegistry.getOrDefault(AuditService.class, new AuditServiceImpl(auditDao)));

        // Stubs first, so the application always runs. Teammates replace them
        // with their real implementations when they are ready.
        ServiceRegistry.registerDefaults();
        ServiceRegistry.register(AuditService.class, new AuditServiceImpl(auditDao));

        // With a real database the counters come from the real tables instead
        // of the stubs. The owning segment can still replace any of them.
        registerDatabaseBackedServices();
    }

    /**
     * Registers the database-backed implementations of the service contracts.
     *
     * <p>They are only registered when MySQL is available - in memory mode the
     * stubs (or the demo data) stay in place.
     */
    private void registerDatabaseBackedServices() {
        if (itemDao != null && claimDao != null && matchDao != null) {
            ServiceRegistry.register(ItemCatalogService.class, new ItemCatalogServiceImpl(itemDao));
            ServiceRegistry.register(ClaimQueryService.class, new ClaimQueryServiceImpl(claimDao));
            ServiceRegistry.register(MatchQueryService.class, new MatchQueryServiceImpl(matchDao));
            ServiceRegistry.register(AdminWorkloadService.class,
                    new AdminWorkloadServiceImpl(itemDao, claimDao, matchDao));
        }
        if (notificationDao != null) {
            ServiceRegistry.register(NotificationService.class,
                    new NotificationServiceImpl(notificationDao));
        }
    }

    public AppConfig config() {
        return config;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public UserDao userDao() {
        return userDao;
    }

    public AuditDao auditDao() {
        return auditDao;
    }

    public NotificationDao notificationDao() {
        return notificationDao;
    }

    /** Null when the app runs without a database. */
    public ItemDao itemDao() {
        return itemDao;
    }

    /** Null when the app runs without a database. */
    public ClaimDao claimDao() {
        return claimDao;
    }

    /** Null when the app runs without a database. */
    public MatchDao matchDao() {
        return matchDao;
    }

    public SessionManager sessionManager() {
        return sessionManager;
    }

    public AuthService authService() {
        return authService;
    }

    public RbacService rbacService() {
        return rbacService;
    }

    public UserService userService() {
        return userService;
    }

    public DashboardService dashboardService() {
        return dashboardService;
    }

    public SearchService searchService() {
        return searchService;
    }

    public ReportService reportService() {
        return reportService;
    }
}
