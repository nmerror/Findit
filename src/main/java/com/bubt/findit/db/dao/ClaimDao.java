package com.bubt.findit.db.dao;

/**
 * Reads and writes the {@code claims} table.
 *
 * <p>Segment 3 creates the claim, Segment 4 verifies it. The dashboard only
 * counts them through the {@code ClaimQueryService} contract.
 */
public interface ClaimDao {

    /** Number of claims in one status (SUBMITTED, APPROVED, ...). */
    long countByStatus(String status);

    /** Number of claims made by one user. */
    long countByUser(long userId);

    /** Number of claims for one item. */
    long countByItem(long itemId);

    /** Adds a new claim and returns its id. */
    long insert(ClaimRecord claim);

    /** Changes the status of a claim, e.g. when an admin approves it. */
    boolean updateStatus(long claimId, String status, Long verifiedBy, String decisionNote);
}
