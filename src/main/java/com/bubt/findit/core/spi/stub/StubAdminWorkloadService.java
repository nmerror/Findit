package com.bubt.findit.core.spi.stub;

import com.bubt.findit.core.spi.AdminWorkloadService;

/** Placeholder for Segment 4 (admin console) until it exists. */
public class StubAdminWorkloadService implements AdminWorkloadService {

    @Override
    public int countReportsPendingReview() {
        return 0;
    }

    @Override
    public int countClaimsPendingVerification() {
        return 0;
    }

    @Override
    public int countAwaitingReturnHandover() {
        return 0;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
