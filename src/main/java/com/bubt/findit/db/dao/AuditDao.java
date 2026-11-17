package com.bubt.findit.db.dao;

import java.time.LocalDateTime;
import java.util.List;

/** Data access for the {@code audit_log} table (owned by Segment 1). */
public interface AuditDao {

    void insert(AuditEntry entry);

    List<AuditEntry> recent(int limit);

    List<AuditEntry> byActor(long actorUserId, int limit);

    /**
     * DELETE operation: removes every entry older than the given moment.
     *
     * @return the number of deleted rows
     */
    int deleteOlderThan(LocalDateTime cutoff);
}
