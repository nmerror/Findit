package com.bubt.findit.core.spi;

/**
 * ============================================================================
 *  SERVICE CONTRACT - implemented by SEGMENT 4
 *  (Administrative Verification, Claim Approval & Item Return Management)
 * ============================================================================
 *
 * <p>The admin console of Segment 4 is a separate window from the student
 * dashboard of Segment 1. This interface is how Segment 1 learns that an
 * administrator action is waiting.
 */
public interface AdminWorkloadService {

    /** Reports waiting for a first admin look. */
    int countReportsPendingReview();

    /** Claims waiting for evidence verification. */
    int countClaimsPendingVerification();

    /** Items approved but not yet handed back to the owner. */
    int countAwaitingReturnHandover();

    default boolean isAvailable() {
        return true;
    }
}
