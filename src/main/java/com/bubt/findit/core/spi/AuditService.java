package com.bubt.findit.core.spi;

/**
 * ============================================================================
 *  SERVICE CONTRACT - implemented by SEGMENT 1 (it is a Segment 1 feature),
 *  but every other segment may call it to record what they did.
 * ============================================================================
 *
 * <p>Example: Segment 4 calls
 * {@code AuditService.record("CLAIM_APPROVED", adminId, "claim #12 approved")}
 * and the action appears in the admin audit table.
 */
public interface AuditService {

    void record(String action, Long actorUserId, String details);
}
