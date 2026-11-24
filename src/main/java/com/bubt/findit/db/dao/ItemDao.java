package com.bubt.findit.db.dao;

import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.LocationCount;

import java.util.List;

/**
 * Reads and writes the {@code items} and {@code locations} tables.
 *
 * <p>Segment 2 owns these tables. Segment 1 uses this DAO through the
 * {@code ItemCatalogService} contract so that the dashboard can show real
 * numbers without ever writing into somebody else's table.
 */
public interface ItemDao {

    /** Total number of items, optionally only of one type (LOST / FOUND). */
    long countByType(String itemType);

    /** Total number of items in one category. */
    long countByCategory(String category);

    /** Number of items that are still OPEN. */
    long countOpen();

    /** Number of items reported by one user, optionally of one type. */
    long countReportedBy(long userId, String itemType);

    /** The newest items, newest first. */
    List<ItemSummary> recent(int limit);

    /** The busiest locations, e.g. "Central Library - 7 items". */
    List<LocationCount> topLocations(int limit);

    /** Adds a new report and returns its id. */
    long insert(ItemRecord item);
}
