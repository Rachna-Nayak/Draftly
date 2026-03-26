package com.draftly.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "submission_feedback")
public class SubmissionFeedback {

    @Id
    private String id;
    private String submissionId;
    private String reviewerId;
    private String message;
    private LocalDateTime date;

    public SubmissionFeedback() {
        this.date = LocalDateTime.now();
    }

    public SubmissionFeedback(String submissionId, String reviewerId, String message) {
        this();
        this.submissionId = submissionId;
        this.reviewerId = reviewerId;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
