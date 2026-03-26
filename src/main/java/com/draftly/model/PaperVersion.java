package com.draftly.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "paper_versions")
public class PaperVersion {

    @Id
    private String id;
    private String submissionId;
    private int versionNumber;
    private String filePath;
    private String notes;
    private LocalDateTime uploadDate;

    public PaperVersion() {
        this.uploadDate = LocalDateTime.now();
    }

    public PaperVersion(String submissionId, int versionNumber, String filePath, String notes) {
        this();
        this.submissionId = submissionId;
        this.versionNumber = versionNumber;
        this.filePath = filePath;
        this.notes = notes;
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

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
