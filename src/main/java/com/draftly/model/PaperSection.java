package com.draftly.model;

/**
 * Represents a section of a research paper (e.g., Abstract, Introduction, Methodology).
 * Embedded within ResearchPaper.
 */
public class PaperSection {

    private String sectionName;
    private String content;
    private int order;

    public PaperSection() {}

    public PaperSection(String sectionName, String content, int order) {
        this.sectionName = sectionName;
        this.content = content;
        this.order = order;
    }

    // --- Getters and Setters ---

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
