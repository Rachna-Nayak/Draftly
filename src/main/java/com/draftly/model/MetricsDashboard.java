package com.draftly.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Aggregated dashboard metrics for a specific user role or scope.
 */
@Document(collection = "metrics_dashboards")
public class MetricsDashboard {

    @Id
    private String id;
    private String scopeType;
    private String scopeId;

    private int projectsCount;
    private int submissionsCount;
    private int referencesCount;

    private int assignedReviewsCount;
    private int completedReviewsCount;
    private int pendingReviewsCount;

    private int usersCount;
    private int submissionsPerTrackCount;
    private int reviewStatsCount;

    private LocalDateTime lastUpdatedAt;

    public MetricsDashboard() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public MetricsDashboard(String scopeType, String scopeId) {
        this();
        this.scopeType = scopeType;
        this.scopeId = scopeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public int getProjectsCount() {
        return projectsCount;
    }

    public void setProjectsCount(int projectsCount) {
        this.projectsCount = projectsCount;
    }

    public int getSubmissionsCount() {
        return submissionsCount;
    }

    public void setSubmissionsCount(int submissionsCount) {
        this.submissionsCount = submissionsCount;
    }

    public int getReferencesCount() {
        return referencesCount;
    }

    public void setReferencesCount(int referencesCount) {
        this.referencesCount = referencesCount;
    }

    public int getAssignedReviewsCount() {
        return assignedReviewsCount;
    }

    public void setAssignedReviewsCount(int assignedReviewsCount) {
        this.assignedReviewsCount = assignedReviewsCount;
    }

    public int getCompletedReviewsCount() {
        return completedReviewsCount;
    }

    public void setCompletedReviewsCount(int completedReviewsCount) {
        this.completedReviewsCount = completedReviewsCount;
    }

    public int getPendingReviewsCount() {
        return pendingReviewsCount;
    }

    public void setPendingReviewsCount(int pendingReviewsCount) {
        this.pendingReviewsCount = pendingReviewsCount;
    }

    public int getUsersCount() {
        return usersCount;
    }

    public void setUsersCount(int usersCount) {
        this.usersCount = usersCount;
    }

    public int getSubmissionsPerTrackCount() {
        return submissionsPerTrackCount;
    }

    public void setSubmissionsPerTrackCount(int submissionsPerTrackCount) {
        this.submissionsPerTrackCount = submissionsPerTrackCount;
    }

    public int getReviewStatsCount() {
        return reviewStatsCount;
    }

    public void setReviewStatsCount(int reviewStatsCount) {
        this.reviewStatsCount = reviewStatsCount;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
