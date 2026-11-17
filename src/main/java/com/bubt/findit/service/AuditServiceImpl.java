package com.bubt.findit.service;

import com.bubt.findit.core.spi.AuditService;
import com.bubt.findit.db.dao.AuditDao;
import com.bubt.findit.db.dao.AuditEntry;

import java.time.LocalDateTime;

/**
 * Writes to the {@code audit_log} table. Segment 1 owns the table; every
 * segment may call this service to record what it did.
 */
public class AuditServiceImpl implements AuditService {

    private final AuditDao auditDao;

    public AuditServiceImpl(AuditDao auditDao) {
        this.auditDao = auditDao;
    }

    @Override
    public void record(String action, Long actorUserId, String details) {
        try {
            auditDao.insert(new AuditEntry(actorUserId, action, details, LocalDateTime.now()));
        } catch (RuntimeException ex) {
            // Auditing must never break the business operation.
            System.err.println("[audit] could not write '" + action + "': " + ex.getMessage());
        }
    }
}
