package com.draftly.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Author-specific profile linked to a User account.
 */
@Document(collection = "author_profiles")
public class AuthorProfile {

    @Id
    private String id;
    private String userId;
    private String affiliation;
    private List<String> domainInterests = new ArrayList<>();
    private List<String> projectIds = new ArrayList<>();
    private List<String> submissionIds = new ArrayList<>();

    public AuthorProfile() {
    }

    public AuthorProfile(String userId, String affiliation, List<String> domainInterests) {
        this.userId = userId;
        this.affiliation = affiliation;
        if (domainInterests != null) {
            this.domainInterests = domainInterests;
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

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public List<String> getDomainInterests() {
        return domainInterests;
    }

    public void setDomainInterests(List<String> domainInterests) {
        this.domainInterests = domainInterests;
    }

    public List<String> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<String> projectIds) {
        this.projectIds = projectIds;
    }

    public List<String> getSubmissionIds() {
        return submissionIds;
    }

    public void setSubmissionIds(List<String> submissionIds) {
        this.submissionIds = submissionIds;
    }

    public void addProjectId(String projectId) {
        this.projectIds.add(projectId);
    }

    public void addSubmissionId(String submissionId) {
        this.submissionIds.add(submissionId);
    }
}
