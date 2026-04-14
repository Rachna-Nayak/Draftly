package com.draftly.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing the submission context for reviewers.
 * Author information is stripped for blind review purposes.
 */
public class SubmissionReviewContext {

    private String submissionId;
    private String title;
    private String conferenceName;
    private String track;
    private LocalDateTime submissionDate;
    private LocalDateTime reviewDueDate;
    private int totalReviewersAssigned;
    private int completedReviews;
    private String abstractText;
    private List<String> keywords;
    private String docxPath; // Link to download paper only
    private int referenceCount;
    private String status;

    public SubmissionReviewContext() {
    }

    public SubmissionReviewContext(String submissionId, String title, String conferenceName, String track,
            LocalDateTime submissionDate, LocalDateTime reviewDueDate, int totalReviewersAssigned,
            int completedReviews, String abstractText, List<String> keywords, String docxPath,
            int referenceCount, String status) {
        this.submissionId = submissionId;
        this.title = title;
        this.conferenceName = conferenceName;
        this.track = track;
        this.submissionDate = submissionDate;
        this.reviewDueDate = reviewDueDate;
        this.totalReviewersAssigned = totalReviewersAssigned;
        this.completedReviews = completedReviews;
        this.abstractText = abstractText;
        this.keywords = keywords;
        this.docxPath = docxPath;
        this.referenceCount = referenceCount;
        this.status = status;
    }

    // Getters and Setters

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getConferenceName() {
        return conferenceName;
    }

    public void setConferenceName(String conferenceName) {
        this.conferenceName = conferenceName;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public LocalDateTime getReviewDueDate() {
        return reviewDueDate;
    }

    public void setReviewDueDate(LocalDateTime reviewDueDate) {
        this.reviewDueDate = reviewDueDate;
    }

    public int getTotalReviewersAssigned() {
        return totalReviewersAssigned;
    }

    public void setTotalReviewersAssigned(int totalReviewersAssigned) {
        this.totalReviewersAssigned = totalReviewersAssigned;
    }

    public int getCompletedReviews() {
        return completedReviews;
    }

    public void setCompletedReviews(int completedReviews) {
        this.completedReviews = completedReviews;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getDocxPath() {
        return docxPath;
    }

    public void setDocxPath(String docxPath) {
        this.docxPath = docxPath;
    }

    public int getReferenceCount() {
        return referenceCount;
    }

    public void setReferenceCount(int referenceCount) {
        this.referenceCount = referenceCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
