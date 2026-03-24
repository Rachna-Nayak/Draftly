package com.draftly.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a reference saved to a project, including generated citation. (UC4)
 */
@Document(collection = "references")
public class Reference {

    @Id
    private String id;
    private String projectId;
    private String paperId;
    private String citationFormat; // APA, MLA, IEEE
    private String formattedCitation;
    private Integer referenceNumber; // Number used in manuscript citation markers, e.g., [1]
    private String bibtexKey; // Citation key used in .bib, e.g., smith2024

    public Reference() {}

    public Reference(String projectId, String paperId, String citationFormat, String formattedCitation) {
        this.projectId = projectId;
        this.paperId = paperId;
        this.citationFormat = citationFormat;
        this.formattedCitation = formattedCitation;
    }

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getPaperId() { return paperId; }
    public void setPaperId(String paperId) { this.paperId = paperId; }

    public String getCitationFormat() { return citationFormat; }
    public void setCitationFormat(String citationFormat) { this.citationFormat = citationFormat; }

    public String getFormattedCitation() { return formattedCitation; }
    public void setFormattedCitation(String formattedCitation) { this.formattedCitation = formattedCitation; }

    public Integer getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(Integer referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getBibtexKey() { return bibtexKey; }
    public void setBibtexKey(String bibtexKey) { this.bibtexKey = bibtexKey; }
}
