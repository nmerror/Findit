package com.bubt.findit.db.dao;

import java.time.LocalDateTime;

/** One row of the {@code audit_log} table. */
public class AuditEntry {

    private Long id;
    private Long actorUserId;
    private String action;
    private String details;
    private LocalDateTime createdAt;

    public AuditEntry() {
    }

    public AuditEntry(Long actorUserId, String action, String details, LocalDateTime createdAt) {
        this.actorUserId = actorUserId;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
