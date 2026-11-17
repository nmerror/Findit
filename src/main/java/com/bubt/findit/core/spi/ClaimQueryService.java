package com.bubt.findit.core.spi;

import com.bubt.findit.core.model.ItemSummary;

import java.util.List;

/**
 * ============================================================================
 *  SERVICE CONTRACT - implemented by SEGMENT 3
 *  (Intelligent Item Matching, Similarity Detection & Ownership Claims)
 * ============================================================================
 *
 * <p>Segment 3 needs to answer "does this lost item match that found item?"
 * and "who is claiming this item?". Segment 1 only needs the counters, so we
 * expose read-only counts here instead of the whole matching engine.
 */
public interface ClaimQueryService {

    /** Claims submitted but not yet decided. */
    int countOpenClaims();

    /** Claims waiting for an administrator to check the evidence. */
    int countPendingVerification();

    /** Claims approved by an administrator. */
    int countApprovedClaims();

    /** Items whose return has been confirmed. */
    int countReturnedItems();

    /** Claims submitted by one specific user (used by "My Claims" screen). */
    int countClaimsByUser(long userId);

    default boolean isAvailable() {
        return true;
    }
}
