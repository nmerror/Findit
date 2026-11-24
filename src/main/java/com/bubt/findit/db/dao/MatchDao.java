package com.bubt.findit.db.dao;

/**
 * Reads and writes the {@code matches} table.
 *
 * <p>Segment 3 owns the matching engine.
 */
public interface MatchDao {

    /** Number of matches in one status (NEW, CONFIRMED, ...). */
    long countByStatus(String status);

    /** Total number of matches. */
    long countAll();

    /** Adds a new match and returns its id. */
    long insert(MatchRecord match);
}
