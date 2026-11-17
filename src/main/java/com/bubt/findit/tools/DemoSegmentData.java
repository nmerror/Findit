package com.bubt.findit.tools;

import com.bubt.findit.core.model.ItemCategory;
import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.ItemType;
import com.bubt.findit.core.model.LocationCount;
import com.bubt.findit.core.registry.ServiceRegistry;
import com.bubt.findit.core.spi.AdminWorkloadService;
import com.bubt.findit.core.spi.ClaimQueryService;
import com.bubt.findit.core.spi.ItemCatalogService;
import com.bubt.findit.core.spi.MatchQueryService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 *  DEMO DATA - lets you present a fully populated dashboard today
 * ============================================================================
 *
 * <p>Until Segments 2, 3 and 4 are finished, the dashboard shows zeros. This
 * class registers <b>fake</b> implementations of their contracts so the
 * dashboard looks complete:
 *
 * <pre>
 *   java -cp target/classes com.bubt.findit.Main --demo
 * </pre>
 *
 * <p>The moment the real implementations exist, delete the call to
 * {@link #install()} and the real numbers take over automatically - that is
 * the whole point of the service registry.
 */
public final class DemoSegmentData {

    private DemoSegmentData() {
    }

    public static void install() {
        ServiceRegistry.register(ItemCatalogService.class, new DemoItemCatalog());
        ServiceRegistry.register(ClaimQueryService.class, new DemoClaimQuery());
        ServiceRegistry.register(MatchQueryService.class, new DemoMatchQuery());
        ServiceRegistry.register(AdminWorkloadService.class, new DemoAdminWorkload());
        System.out.println("[demo] fake Segment 2/3/4 data installed (dashboard will look full)");
    }

    /** Fake Segment 2. */
    static final class DemoItemCatalog implements ItemCatalogService {

        private final List<ItemSummary> items = new ArrayList<>();

        DemoItemCatalog() {
            items.add(summary(11, "Black leather wallet", ItemCategory.ACCESSORIES, ItemType.LOST,
                    "Central Library", "Ayesha Rahman", 2));
            items.add(summary(12, "Blue PS4 controller", ItemCategory.ELECTRONICS, ItemType.LOST,
                    "Exam Hall", "Tanvir Ahmed", 3));
            items.add(summary(13, "Silver wrist watch", ItemCategory.ACCESSORIES, ItemType.LOST,
                    "Sports Complex", "Nusrat Jahan", 4));
            items.add(summary(14, "Student ID card", ItemCategory.DOCUMENTS, ItemType.FOUND,
                    "Cafeteria", "Tanvir Ahmed", 3));
            items.add(summary(15, "Black wireless earphones", ItemCategory.ELECTRONICS, ItemType.FOUND,
                    "Central Library", "Nusrat Jahan", 4));
            items.add(summary(16, "Calculus textbook", ItemCategory.BOOKS, ItemType.FOUND,
                    "Main Auditorium", "Ayesha Rahman", 2));
        }

        private ItemSummary summary(long id, String title, ItemCategory category, ItemType type,
                                    String location, String postedBy, long userId) {
            return new ItemSummary(id, title, category.name(), type, location, postedBy, userId,
                    "OPEN", LocalDateTime.now().minusHours(id));
        }

        @Override
        public List<ItemSummary> recentItems(int limit) {
            return items.size() > limit ? items.subList(0, limit) : items;
        }

        @Override
        public int countByType(ItemType type) {
            return (int) items.stream().filter(i -> i.getType() == type).count();
        }

        @Override
        public int countByCategory(ItemCategory category) {
            return (int) items.stream().filter(i -> i.getCategory().equals(category.name())).count();
        }

        @Override
        public List<LocationCount> topLocations(int limit) {
            return List.of(new LocationCount("Central Library", 2),
                    new LocationCount("Exam Hall", 1),
                    new LocationCount("Cafeteria", 1),
                    new LocationCount("Sports Complex", 1));
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    /** Fake Segment 3 (claims). */
    static final class DemoClaimQuery implements ClaimQueryService {

        @Override
        public int countOpenClaims() {
            return 4;
        }

        @Override
        public int countPendingVerification() {
            return 2;
        }

        @Override
        public int countApprovedClaims() {
            return 7;
        }

        @Override
        public int countReturnedItems() {
            return 5;
        }

        @Override
        public int countClaimsByUser(long userId) {
            return 2;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    /** Fake Segment 3 (matching engine). */
    static final class DemoMatchQuery implements MatchQueryService {

        @Override
        public int countNewMatches() {
            return 3;
        }

        @Override
        public int countConfirmedMatches() {
            return 6;
        }

        @Override
        public int countIndexedItems() {
            return 18;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    /** Fake Segment 4 (admin console). */
    static final class DemoAdminWorkload implements AdminWorkloadService {

        @Override
        public int countReportsPendingReview() {
            return 3;
        }

        @Override
        public int countClaimsPendingVerification() {
            return 2;
        }

        @Override
        public int countAwaitingReturnHandover() {
            return 1;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}
