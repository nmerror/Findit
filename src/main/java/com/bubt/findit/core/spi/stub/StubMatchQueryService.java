package com.bubt.findit.core.spi.stub;

import com.bubt.findit.core.spi.MatchQueryService;

/** Placeholder for Segment 3 (matching engine) until it exists. */
public class StubMatchQueryService implements MatchQueryService {

    @Override
    public int countNewMatches() {
        return 0;
    }

    @Override
    public int countConfirmedMatches() {
        return 0;
    }

    @Override
    public int countIndexedItems() {
        return 0;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
