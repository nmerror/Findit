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

    /**
     * The posts of one type and one status, newest first.
     *
     * @param itemType LOST or FOUND, or null for both
     * @param status   OPEN, CLOSED ... or null for every status
     */
    List<ItemSummary> findByType(String itemType, String status, int limit);

    /** The posts of one user, newest first. */
    List<ItemSummary> findByReporter(long userId, int limit);

    /** One post, or null when it does not exist. */
    ItemSummary findById(long itemId);

    /** Changes the status of a post - "CLOSED" when the owner found the item. */
    boolean updateStatus(long itemId, String status);

    /** DELETE: removes the post and its photo. */
    boolean deleteById(long itemId);

    /** Stores the photo of a post. */
    boolean insertImage(long itemId, long userId, String filePath);

    /** The photo of a post, or null when the user attached none. */
    String findImagePath(long itemId);

    /** The names of the campus locations - for the drop-down of the form. */
    List<String> listLocations();
}
