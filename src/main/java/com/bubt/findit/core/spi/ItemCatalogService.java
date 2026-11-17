package com.bubt.findit.core.spi;

import com.bubt.findit.core.model.ItemCategory;
import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.ItemType;
import com.bubt.findit.core.model.LocationCount;

import java.util.List;

/**
 * ============================================================================
 *  SERVICE CONTRACT - implemented by SEGMENT 2
 *  (Lost & Found Item Reporting, Image Upload & Location Management)
 * ============================================================================
 *
 * <p>Segment 1 (the dashboard) needs to show "how many items were lost/found"
 * and "what was reported recently". Segment 1 must NOT read the {@code items}
 * table directly, because then the two segments are coupled through SQL and any
 * schema change breaks both sides.
 *
 * <p>So Segment 1 declares the interface below, Segment 2 implements it, and
 * the implementation is registered at start-up (see {@code ServiceRegistry}).
 *
 * <p>Segment 2 team's checklist:
 * <ol>
 *   <li>Create {@code com.bubt.findit.segment2.service.ItemCatalogServiceImpl}
 *       implementing this interface.</li>
 *   <li>Register it in {@code ServiceRegistry.register(ItemCatalogService.class, impl)}
 *       (do it from your own bootstrap class, not inside Segment 1).</li>
 *   <li>Never change this interface without telling the Segment 1 owner.</li>
 * </ol>
 */
public interface ItemCatalogService {

    /** The 10 most recent items, newest first. Never return null. */
    List<ItemSummary> recentItems(int limit);

    /** Total number of LOST or FOUND items. */
    int countByType(ItemType type);

    /** Total number of items in one category. */
    int countByCategory(ItemCategory category);

    /** Busiest campus locations, e.g. "Library - 12 items". */
    List<LocationCount> topLocations(int limit);

    /**
     * True when the real Segment 2 implementation is active. The dashboard
     * shows a "waiting for Segment 2" hint while this is false.
     */
    default boolean isAvailable() {
        return true;
    }
}
