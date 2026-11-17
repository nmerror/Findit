package com.bubt.findit.db.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory implementation of {@link AuditDao}. */
public class InMemoryAuditDao implements AuditDao {

    private final List<AuditEntry> entries = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public void insert(AuditEntry entry) {
        entry.setId(sequence.incrementAndGet());
        if (entry.getCreatedAt() == null) {
            entry.setCreatedAt(LocalDateTime.now());
        }
        entries.add(entry);
    }

    @Override
    public List<AuditEntry> recent(int limit) {
        List<AuditEntry> all = new ArrayList<>(entries);
        Collections.reverse(all);
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    @Override
    public int deleteOlderThan(LocalDateTime cutoff) {
        int before = entries.size();
        entries.removeIf(entry -> entry.getCreatedAt() != null && entry.getCreatedAt().isBefore(cutoff));
        return before - entries.size();
    }

    @Override
    public List<AuditEntry> byActor(long actorUserId, int limit) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry entry : entries) {
            if (entry.getActorUserId() != null && entry.getActorUserId() == actorUserId) {
                result.add(entry);
            }
        }
        Collections.reverse(result);
        return result.size() > limit ? result.subList(0, limit) : result;
    }
}
