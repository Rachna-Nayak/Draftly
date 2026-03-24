package com.draftly.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a research paper being written by the student.
 * Contains sections that can be reviewed/exported. (UC6, UC7, UC8)
 */
@Document(collection = "research_papers")
public class ResearchPaper {

    @Id
    private String id;
    private String projectId;
    private String title;
    private String template; // IEEE, LNCS
    private List<PaperSection> sections = new ArrayList<>();
    private List<Feedback> feedbacks = new ArrayList<>();
    private String status; // DRAFT, IN_REVIEW, APPROVED, EXPORTED

    public ResearchPaper() {
        this.status = "DRAFT";
        this.template = "IEEE";
    }

    public ResearchPaper(String projectId, String title) {
        this();
        this.projectId = projectId;
        this.title = title;
    }

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public List<PaperSection> getSections() { return sections; }
    public void setSections(List<PaperSection> sections) { this.sections = sections; }

    public List<Feedback> getFeedbacks() { return feedbacks; }
    public void setFeedbacks(List<Feedback> feedbacks) { this.feedbacks = feedbacks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void addSection(PaperSection section) { this.sections.add(section); }
    public void addFeedback(Feedback feedback) { this.feedbacks.add(feedback); }
}
