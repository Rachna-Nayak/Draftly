package com.draftly.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reviews")
public class Review {

    public enum Decision {
        // Legacy decisions (maintained for backward compatibility)
        ACCEPT,
        REVISION,
        REJECT,
        // New 5-level descriptive decisions
        STRONG_ACCEPT,
        BORDERLINE,
        STRONG_REJECT
    }

    public enum Confidence {
        HIGH,
        MEDIUM,
        LOW
    }

    @Id
    private String id;
    private String submissionId;
    private String reviewerId;
    private int rating;
    private String comments;
    private Decision decision;
    private LocalDateTime reviewDate;

    // Optional category ratings (1-5 scale)
    private Integer technicalQuality;
    private Integer clarity;
    private Integer originality;
    private Integer significance;

    // Reviewer confidence level
    private Confidence confidence;

    public Review() {
        this.reviewDate = LocalDateTime.now();
    }

    public Review(String submissionId, String reviewerId, int rating, String comments, Decision decision) {
        this();
        this.submissionId = submissionId;
        this.reviewerId = reviewerId;
        this.rating = rating;
        this.comments = comments;
        this.decision = decision;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
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

    public Confidence getConfidence() {
        return confidence;
    }

    public void setConfidence(Confidence confidence) {
        this.confidence = confidence;
    }
}
