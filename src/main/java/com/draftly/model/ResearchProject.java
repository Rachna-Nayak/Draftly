package com.draftly.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a research project created by a student. (UC1)
 */
@Document(collection = "projects")
public class ResearchProject {

    @Id
    private String id;
    private String title;
    private String domain;
    private List<String> keywords = new ArrayList<>();
    private String objectives;
    private String ownerId; // User ID of the student
    private List<String> paperIds = new ArrayList<>();
    private List<String> referenceIds = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResearchProject() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ResearchProject(String title, String domain, List<String> keywords, String objectives, String ownerId) {
        this();
        this.title = title;
        this.domain = domain;
        this.keywords = keywords;
        this.objectives = objectives;
        this.ownerId = ownerId;
    }

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public List<String> getPaperIds() { return paperIds; }
    public void setPaperIds(List<String> paperIds) { this.paperIds = paperIds; }

    public List<String> getReferenceIds() { return referenceIds; }
    public void setReferenceIds(List<String> referenceIds) { this.referenceIds = referenceIds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void addPaperId(String paperId) { this.paperIds.add(paperId); }
    public void addReferenceId(String referenceId) { this.referenceIds.add(referenceId); }
}
