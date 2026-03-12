package com.draftly.model;

import java.time.LocalDateTime;

/**
 * Represents feedback from a faculty/supervisor on a paper section. (UC6)
 * Embedded within ResearchPaper.
 */
public class Feedback {

    private String facultyId;
    private String facultyName;
    private String sectionName;
    private String comment;
    private String status; // APPROVED, REVISION_NEEDED
    private LocalDateTime createdAt;

    public Feedback() {
        this.createdAt = LocalDateTime.now();
    }

    public Feedback(String facultyId, String facultyName, String sectionName, String comment, String status) {
        this();
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.sectionName = sectionName;
        this.comment = comment;
        this.status = status;
    }

    // --- Getters and Setters ---

    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
