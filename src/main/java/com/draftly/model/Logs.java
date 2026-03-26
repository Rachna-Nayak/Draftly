package com.draftly.model;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * Base model for platform logs.
 * Intended to be extended by specific log types like change logs and notification logs.
 */
public abstract class Logs {

    @Id
    private String id;
    private String userId;
    private String actionType;
    private String message;
    private LocalDateTime createdAt;

    public Logs() {
        this.createdAt = LocalDateTime.now();
    }

    public Logs(String userId, String actionType, String message) {
        this();
        this.userId = userId;
        this.actionType = actionType;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
