package com.bubt.findit.core.spi;

import com.bubt.findit.core.model.SearchHit;

import java.util.List;

/**
 * ============================================================================
 *  OPTIONAL SERVICE CONTRACT - any segment may implement this
 * ============================================================================
 *
 * <p>The global search in the top bar searches the whole system. Segment 1
 * searches the {@code users} table itself; every other segment can join in by
 * registering a contributor:
 *
 * <pre>{@code
 * ServiceRegistry.registerSearchContributor(new ItemSearchContributor());
 * }</pre>
 *
 * <p>Because this contract is <b>optional</b>, nobody is forced to implement
 * it, and Segment 1 never imports a class from another segment.
 */
public interface SearchContributor {

    /**
     * Returns the rows of this segment that match the query.
     *
     * @param query what the user typed (never null, never blank)
     * @param limit maximum number of hits to return
     */
    List<SearchHit> search(String query, int limit);

    /** The name shown in the "source" tag of the hit, e.g. "ITEMS". */
    String sourceName();

    /** False while the implementation is still a stub. */
    default boolean isAvailable() {
        return true;
    }
}
