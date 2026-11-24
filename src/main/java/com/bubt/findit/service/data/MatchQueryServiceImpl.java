package com.bubt.findit.service.data;

import com.bubt.findit.core.spi.MatchQueryService;
import com.bubt.findit.db.dao.MatchDao;

/**
 * Reads the match counters of Segment 3 straight from the database.
 *
 * <p>Reference implementation of the {@code MatchQueryService} contract.
 */
public class MatchQueryServiceImpl implements MatchQueryService {

    public static final String NEW = "NEW";
    public static final String CONFIRMED = "CONFIRMED";

    private final MatchDao matchDao;

    public MatchQueryServiceImpl(MatchDao matchDao) {
        this.matchDao = matchDao;
    }

    @Override
    public int countNewMatches() {
        return (int) matchDao.countByStatus(NEW);
    }

    @Override
    public int countConfirmedMatches() {
        return (int) matchDao.countByStatus(CONFIRMED);
    }

    @Override
    public int countIndexedItems() {
        return (int) matchDao.countAll();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
