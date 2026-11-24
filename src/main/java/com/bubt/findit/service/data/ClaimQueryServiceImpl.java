package com.bubt.findit.service.data;

import com.bubt.findit.core.spi.ClaimQueryService;
import com.bubt.findit.db.dao.ClaimDao;

/**
 * Reads the claim counters of Segment 3 straight from the database.
 *
 * <p>Reference implementation of the {@code ClaimQueryService} contract.
 */
public class ClaimQueryServiceImpl implements ClaimQueryService {

    /** The claim statuses of the schema. */
    public static final String SUBMITTED = "SUBMITTED";
    public static final String UNDER_REVIEW = "UNDER_REVIEW";
    public static final String APPROVED = "APPROVED";
    public static final String RETURNED = "RETURNED";

    private final ClaimDao claimDao;

    public ClaimQueryServiceImpl(ClaimDao claimDao) {
        this.claimDao = claimDao;
    }

    @Override
    public int countOpenClaims() {
        return (int) claimDao.countByStatus(SUBMITTED);
    }

    @Override
    public int countPendingVerification() {
        return (int) claimDao.countByStatus(UNDER_REVIEW);
    }

    @Override
    public int countApprovedClaims() {
        return (int) claimDao.countByStatus(APPROVED);
    }

    @Override
    public int countReturnedItems() {
        return (int) claimDao.countByStatus(RETURNED);
    }

    @Override
    public int countClaimsByUser(long userId) {
        return (int) claimDao.countByUser(userId);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
