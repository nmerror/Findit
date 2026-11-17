package com.bubt.findit.app;

import com.bubt.findit.config.AppConfig;
import com.bubt.findit.core.event.EventBus;
import com.bubt.findit.core.registry.ServiceRegistry;
import com.bubt.findit.core.spi.AuditService;
import com.bubt.findit.db.DaoFactory;
import com.bubt.findit.db.dao.AuditDao;
import com.bubt.findit.db.dao.UserDao;
import com.bubt.findit.service.AuditServiceImpl;
import com.bubt.findit.service.AuthService;
import com.bubt.findit.service.DashboardService;
import com.bubt.findit.service.RbacService;
import com.bubt.findit.service.ReportService;
import com.bubt.findit.service.SearchService;
import com.bubt.findit.service.SessionManager;
import com.bubt.findit.service.UserService;

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
