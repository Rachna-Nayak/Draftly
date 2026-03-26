package com.draftly.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a conference and its submission/review tracks.
 */
@Document(collection = "conferences")
public class Conference {

    @Id
    private String id;
    private String name;
    private String acronym;
    private String domain;
    private String description;
    private LocalDate submissionDeadline;
    private LocalDate reviewDeadline;
    private List<String> trackNames = new ArrayList<>();
    private List<String> reviewerIds = new ArrayList<>();

    public Conference() {
    }

    public Conference(String name,
                      String acronym,
                      String domain,
                      String description,
                      LocalDate submissionDeadline,
                      LocalDate reviewDeadline,
                      List<String> trackNames) {
        this.name = name;
        this.acronym = acronym;
        this.domain = domain;
        this.description = description;
        this.submissionDeadline = submissionDeadline;
        this.reviewDeadline = reviewDeadline;
        if (trackNames != null) {
            this.trackNames = trackNames;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getSubmissionDeadline() {
        return submissionDeadline;
    }

    public void setSubmissionDeadline(LocalDate submissionDeadline) {
        this.submissionDeadline = submissionDeadline;
    }

    public LocalDate getReviewDeadline() {
        return reviewDeadline;
    }

    public void setReviewDeadline(LocalDate reviewDeadline) {
        this.reviewDeadline = reviewDeadline;
    }

    public List<String> getTrackNames() {
        return trackNames;
    }

    public void setTrackNames(List<String> trackNames) {
        this.trackNames = trackNames;
    }

    public List<String> getReviewerIds() {
        return reviewerIds;
    }

    public void setReviewerIds(List<String> reviewerIds) {
        this.reviewerIds = reviewerIds;
    }

    public void addTrackName(String trackName) {
        this.trackNames.add(trackName);
    }

    public void addReviewerId(String reviewerId) {
        this.reviewerIds.add(reviewerId);
    }
}
