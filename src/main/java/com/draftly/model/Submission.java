package com.draftly.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "submissions")
public class Submission {

    public enum Status {
        DRAFT,
        SUBMITTED,
        UNDER_REVIEW,
        REVISION_REQUIRED,
        ACCEPTED,
        REJECTED,
        PUBLISHED
    }

    @Id
    private String id;
    private String projectId;
    private String authorId;
    private String title;
    private String conferenceName;
    private String track;
    private String docxPath;
    private Status status;
    private boolean locked;
    private List<String> referenceIds = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Submission() {
        this.status = Status.DRAFT;
        this.locked = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Submission(String projectId, String authorId, String title, String conferenceName, String track, String docxPath) {
        this();
        this.projectId = projectId;
        this.authorId = authorId;
        this.title = title;
        this.conferenceName = conferenceName;
        this.track = track;
        this.docxPath = docxPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
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

    public String getDocxPath() {
        return docxPath;
    }

    public void setDocxPath(String docxPath) {
        this.docxPath = docxPath;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public List<String> getReferenceIds() {
        return referenceIds;
    }

    public void setReferenceIds(List<String> referenceIds) {
        this.referenceIds = referenceIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addReferenceId(String referenceId) {
        this.referenceIds.add(referenceId);
        this.updatedAt = LocalDateTime.now();
    }
}
