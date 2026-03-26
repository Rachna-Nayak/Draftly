package com.draftly.model;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Log entry for notification delivery events.
 */
@Document(collection = "notification_logs")
public class NotificationLog extends Logs {

    private String recipientUserId;
    private String channel;
    private String status;
    private LocalDateTime deliveredAt;

    public NotificationLog() {
        super();
    }

    public NotificationLog(String userId, String actionType, String message,
                           String recipientUserId, String channel, String status) {
        super(userId, actionType, message);
        this.recipientUserId = recipientUserId;
        this.channel = channel;
        this.status = status;
        this.deliveredAt = LocalDateTime.now();
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}
