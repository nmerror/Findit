package com.bubt.findit.core.spi.stub;

import com.bubt.findit.core.spi.ClaimQueryService;

/** Placeholder for Segment 3 (claims) until the real implementation exists. */
public class StubClaimQueryService implements ClaimQueryService {

    @Override
    public int countOpenClaims() {
        return 0;
    }

    @Override
    public int countPendingVerification() {
        return 0;
    }

    @Override
    public int countApprovedClaims() {
        return 0;
    }

    @Override
    public int countReturnedItems() {
        return 0;
    }

    @Override
    public int countClaimsByUser(long userId) {
        return 0;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
