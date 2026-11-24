package com.bubt.findit.service.data;

import com.bubt.findit.core.spi.AdminWorkloadService;
import com.bubt.findit.db.dao.ClaimDao;
import com.bubt.findit.db.dao.ItemDao;
import com.bubt.findit.db.dao.MatchDao;

/**
 * Reads the admin workload of Segment 4 straight from the database.
 *
 * <p>Reference implementation of the {@code AdminWorkloadService} contract:
 * it answers "what is still waiting for an administrator?".
 */
public class AdminWorkloadServiceImpl implements AdminWorkloadService {

    private final ItemDao itemDao;
    private final ClaimDao claimDao;
    private final MatchDao matchDao;

    public AdminWorkloadServiceImpl(ItemDao itemDao, ClaimDao claimDao, MatchDao matchDao) {
        this.itemDao = itemDao;
        this.claimDao = claimDao;
        this.matchDao = matchDao;
    }

    @Override
    public int countReportsPendingReview() {
        // Reports that are still OPEN and have not been matched or returned yet.
        return (int) itemDao.countOpen();
    }

    @Override
    public int countClaimsPendingVerification() {
        return (int) claimDao.countByStatus(ClaimQueryServiceImpl.UNDER_REVIEW);
    }

    @Override
    public int countAwaitingReturnHandover() {
        // Approved claims that have not been handed back to their owner yet.
        return (int) claimDao.countByStatus(ClaimQueryServiceImpl.APPROVED)
                + (int) matchDao.countByStatus(MatchQueryServiceImpl.CONFIRMED);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
