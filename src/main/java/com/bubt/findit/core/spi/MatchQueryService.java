package com.bubt.findit.core.spi;

/**
 * ============================================================================
 *  SERVICE CONTRACT - implemented by SEGMENT 3
 *  (Intelligent Item Matching, Similarity Detection)
 * ============================================================================
 *
 * <p>Kept separate from {@link ClaimQueryService} so that the matching engine
 * can be finished before or after the claim workflow - they are different
 * parts of Segment 3's work.
 */
public interface MatchQueryService {

    /** Pairs the matcher found but nobody looked at yet. */
    int countNewMatches();

    /** Pairs the matcher found with a score above the threshold. */
    int countConfirmedMatches();

    /** Number of items that have been through the matcher at least once. */
    int countIndexedItems();

    default boolean isAvailable() {
        return true;
    }
}
