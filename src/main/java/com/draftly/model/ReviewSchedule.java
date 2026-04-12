package com.draftly.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "review_schedules")
public class ReviewSchedule {

    public enum Status {
        SCHEDULED,
        COMPLETED
    }

    @Id
    private String id;
    private String conferenceId;
    private String submissionId;
    private String reviewerUserId;
    private LocalDateTime scheduledAt;
    private LocalDateTime dueAt;
    private String adminUserId;
    private Status status;

    public ReviewSchedule() {
        this.status = Status.SCHEDULED;
    }

    public ReviewSchedule(String conferenceId, String submissionId, String reviewerUserId,
                          LocalDateTime scheduledAt, LocalDateTime dueAt, String adminUserId) {
        this();
        this.conferenceId = conferenceId;
        this.submissionId = submissionId;
        this.reviewerUserId = reviewerUserId;
        this.scheduledAt = scheduledAt;
        this.dueAt = dueAt;
        this.adminUserId = adminUserId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConferenceId() { return conferenceId; }
    public void setConferenceId(String conferenceId) { this.conferenceId = conferenceId; }
    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }
    public String getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(String reviewerUserId) { this.reviewerUserId = reviewerUserId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
    public String getAdminUserId() { return adminUserId; }
    public void setAdminUserId(String adminUserId) { this.adminUserId = adminUserId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}