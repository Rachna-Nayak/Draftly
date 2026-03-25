package com.draftly.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Reviewer-specific profile linked to a User account.
 * Keeps reviewer metadata separate from the core User model.
 */
@Document(collection = "reviewer_profiles")
public class ReviewerProfile {

    @Id
    private String id;
    private String userId;
    private List<String> expertiseDomains = new ArrayList<>();
    private List<String> assignedSubmissionIds = new ArrayList<>();
    private int completedReviews;
    private int pendingReviews;

    public ReviewerProfile() {
    }

    public ReviewerProfile(String userId, List<String> expertiseDomains) {
        this.userId = userId;
        if (expertiseDomains != null) {
            this.expertiseDomains = expertiseDomains;
        }
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

    public List<String> getExpertiseDomains() {
        return expertiseDomains;
    }

    public void setExpertiseDomains(List<String> expertiseDomains) {
        this.expertiseDomains = expertiseDomains;
    }

    public List<String> getAssignedSubmissionIds() {
        return assignedSubmissionIds;
    }

    public void setAssignedSubmissionIds(List<String> assignedSubmissionIds) {
        this.assignedSubmissionIds = assignedSubmissionIds;
    }

    public int getCompletedReviews() {
        return completedReviews;
    }

    public void setCompletedReviews(int completedReviews) {
        this.completedReviews = completedReviews;
    }

    public int getPendingReviews() {
        return pendingReviews;
    }

    public void setPendingReviews(int pendingReviews) {
        this.pendingReviews = pendingReviews;
    }

    public void addAssignedSubmissionId(String submissionId) {
        this.assignedSubmissionIds.add(submissionId);
        this.pendingReviews++;
    }

    public void markReviewCompleted() {
        if (this.pendingReviews > 0) {
            this.pendingReviews--;
        }
        this.completedReviews++;
    }
}
