package com.draftly.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.draftly.model.Review;

/**
 * DTO for submitting a review.
 * Contains validation rules for review submission.
 */
public class ReviewSubmissionRequest {

    @NotBlank(message = "Submission ID is required")
    private String submissionId;

    @NotBlank(message = "Reviewer ID is required")
    private String reviewerId;

    @NotNull(message = "Review decision is required")
    private Review.Decision reviewDecision;

    @NotBlank(message = "Comments are required")
    @Size(min = 50, message = "Comments must be at least 50 characters")
    private String comments;

    // Optional category ratings (1-5 scale)
    @Min(value = 1, message = "Technical Quality must be between 1 and 5")
    @Max(value = 5, message = "Technical Quality must be between 1 and 5")
    private Integer technicalQuality;

    @Min(value = 1, message = "Clarity must be between 1 and 5")
    @Max(value = 5, message = "Clarity must be between 1 and 5")
    private Integer clarity;

    @Min(value = 1, message = "Originality must be between 1 and 5")
    @Max(value = 5, message = "Originality must be between 1 and 5")
    private Integer originality;

    @Min(value = 1, message = "Significance must be between 1 and 5")
    @Max(value = 5, message = "Significance must be between 1 and 5")
    private Integer significance;

    // Reviewer confidence level
    private Review.Confidence confidence;

    public ReviewSubmissionRequest() {
    }

    public ReviewSubmissionRequest(String submissionId, String reviewerId, Review.Decision reviewDecision,
            String comments) {
        this.submissionId = submissionId;
        this.reviewerId = reviewerId;
        this.reviewDecision = reviewDecision;
        this.comments = comments;
    }

    // Getters and Setters

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

    public Review.Decision getReviewDecision() {
        return reviewDecision;
    }

    public void setReviewDecision(Review.Decision reviewDecision) {
        this.reviewDecision = reviewDecision;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getTechnicalQuality() {
        return technicalQuality;
    }

    public void setTechnicalQuality(Integer technicalQuality) {
        this.technicalQuality = technicalQuality;
    }

    public Integer getClarity() {
        return clarity;
    }

    public void setClarity(Integer clarity) {
        this.clarity = clarity;
    }

    public Integer getOriginality() {
        return originality;
    }

    public void setOriginality(Integer originality) {
        this.originality = originality;
    }

    public Integer getSignificance() {
        return significance;
    }

    public void setSignificance(Integer significance) {
        this.significance = significance;
    }

    public Review.Confidence getConfidence() {
        return confidence;
    }

    public void setConfidence(Review.Confidence confidence) {
        this.confidence = confidence;
    }
}
