package com.draftly.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Audit log for tracking entity changes across the platform.
 */
@Document(collection = "change_logs")
public class ChangeLog extends Logs {

    private String entityType;
    private String entityId;
    private String changeSummary;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;

    public ChangeLog() {
        super();
        this.changedAt = LocalDateTime.now();
    }

    public ChangeLog(String userId,
                     String actionType,
                     String message,
                     String entityType,
                     String entityId,
                     String changeSummary,
                     String oldValue,
                     String newValue) {
        super(userId, actionType, message);
        this.entityType = entityType;
        this.entityId = entityId;
        this.changeSummary = changeSummary;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = LocalDateTime.now();
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
