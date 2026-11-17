package com.bubt.findit.service;

import com.bubt.findit.core.model.DashboardStats;
import com.bubt.findit.core.model.ItemType;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.model.UserStatus;
import com.bubt.findit.core.registry.ServiceRegistry;
import com.bubt.findit.core.spi.AdminWorkloadService;
import com.bubt.findit.core.spi.ClaimQueryService;
import com.bubt.findit.core.spi.ItemCatalogService;
import com.bubt.findit.core.spi.MatchQueryService;
import com.bubt.findit.db.dao.UserDao;

import java.util.List;

/**
 * ============================================================================
 *  SEGMENT 1 - Dashboard Service
 * ============================================================================
 *
 * <p>This is the place where Segment 1 touches Segments 2, 3 and 4 - and it
 * does so <b>only through the interfaces in {@code core.spi}</b>.
 *
 * <p>Look at the code below: there is not a single reference to a Segment 2,
 * 3 or 4 class. When their implementations are registered in
 * {@code ServiceRegistry}, the numbers on the dashboard light up automatically.
 */
public class DashboardService {

    private final UserDao userDao;

    public DashboardService(UserDao userDao) {
        this.userDao = userDao;
    }

    public DashboardStats build(User viewer) {
        DashboardStats stats = new DashboardStats();

        // --- Segment 1 owns this data -------------------------------------
        stats.setTotalUsers((int) userDao.countAll());
        stats.setTotalStudents((int) userDao.countByRole(com.bubt.findit.core.model.Role.STUDENT));
        stats.setTotalAdmins((int) userDao.countByRole(com.bubt.findit.core.model.Role.ADMIN));
        stats.setActiveUsers((int) userDao.countByStatus(UserStatus.ACTIVE));
        stats.setSuspendedUsers((int) userDao.countByStatus(UserStatus.SUSPENDED));

        if (viewer != null && viewer.getId() != null) {
            stats.setMyLostReports((int) userDao.countReportsByUser(viewer.getId(), ItemType.LOST.name()));
            stats.setMyFoundReports((int) userDao.countReportsByUser(viewer.getId(), ItemType.FOUND.name()));
        }

        // --- Segment 2 (items) --------------------------------------------
        ItemCatalogService catalog = ServiceRegistry.get(ItemCatalogService.class);
        if (catalog != null && catalog.isAvailable()) {
            stats.setTotalLostItems(catalog.countByType(ItemType.LOST));
            stats.setTotalFoundItems(catalog.countByType(ItemType.FOUND));
            stats.setRecentItems(catalog.recentItems(8));
            stats.setTopLocations(catalog.topLocations(5));
        }

        // --- Segment 3 (matching + claims) -------------------------------
        MatchQueryService matcher = ServiceRegistry.get(MatchQueryService.class);
        if (matcher != null && matcher.isAvailable()) {
            stats.setNewMatches(matcher.countNewMatches());
        }
        ClaimQueryService claims = ServiceRegistry.get(ClaimQueryService.class);
        if (claims != null && claims.isAvailable()) {
            stats.setOpenClaims(claims.countOpenClaims());
            stats.setPendingVerification(claims.countPendingVerification());
            stats.setApprovedClaims(claims.countApprovedClaims());
            stats.setReturnedItems(claims.countReturnedItems());
        }

        // --- Segment 4 (admin workload) -----------------------------------
        AdminWorkloadService workload = ServiceRegistry.get(AdminWorkloadService.class);
        if (workload != null && workload.isAvailable() && viewer != null && viewer.isAdmin()) {
            stats.setPendingVerification(stats.getPendingVerification()
                    + workload.countClaimsPendingVerification());
            stats.setReturnedItems(stats.getReturnedItems()
                    + workload.countAwaitingReturnHandover());
        }

        return stats;
    }

    /** True when every other segment has delivered its implementation. */
    public boolean allSegmentsReady() {
        return isReady(ItemCatalogService.class)
                && isReady(ClaimQueryService.class)
                && isReady(MatchQueryService.class)
                && isReady(AdminWorkloadService.class);
    }

    private boolean isReady(Class<?> contract) {
        Object service = ServiceRegistry.get(contract);
        if (service == null) {
            return false;
        }
        return switch (contract.getSimpleName()) {
            case "ItemCatalogService" -> ((ItemCatalogService) service).isAvailable();
            case "ClaimQueryService" -> ((ClaimQueryService) service).isAvailable();
            case "MatchQueryService" -> ((MatchQueryService) service).isAvailable();
            case "AdminWorkloadService" -> ((AdminWorkloadService) service).isAvailable();
            default -> true;
        };
    }

    /** Which segments are still missing - shown on the dashboard footer. */
    public List<String> missingSegments() {
        java.util.List<String> missing = new java.util.ArrayList<>();
        if (!isReady(ItemCatalogService.class)) {
            missing.add("Segment 2 - Item Reporting");
        }
        if (!isReady(MatchQueryService.class)) {
            missing.add("Segment 3 - Matching Engine");
        }
        if (!isReady(ClaimQueryService.class)) {
            missing.add("Segment 3 - Claims");
        }
        if (!isReady(AdminWorkloadService.class)) {
            missing.add("Segment 4 - Admin Verification");
        }
        return missing;
    }
}
