package com.draftly.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an academic paper in the database. Used for literature search (UC2),
 * credibility evaluation (UC3), and reference management (UC4).
 */
@Document(collection = "papers")
public class Paper {

    @Id
    private String id;
    private String title;
    private List<String> authors = new ArrayList<>();
    private String abstractText;
    private String journal;
    private int publicationYear;
    private int citationCount;
    private double journalImpactFactor;
    private String domain;
    private List<String> keywords = new ArrayList<>();
    private String doi;

    public Paper() {}

    public Paper(String title, List<String> authors, String abstractText, String journal,
                 int publicationYear, int citationCount, double journalImpactFactor,
                 String domain, List<String> keywords, String doi) {
        this.title = title;
        this.authors = authors;
        this.abstractText = abstractText;
        this.journal = journal;
        this.publicationYear = publicationYear;
        this.citationCount = citationCount;
        this.journalImpactFactor = journalImpactFactor;
        this.domain = domain;
        this.keywords = keywords;
        this.doi = doi;
    }

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getAuthors() { return authors; }
    public void setAuthors(List<String> authors) { this.authors = authors; }

    public String getAbstractText() { return abstractText; }
    public void setAbstractText(String abstractText) { this.abstractText = abstractText; }

    public String getJournal() { return journal; }
    public void setJournal(String journal) { this.journal = journal; }

    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }

    public int getCitationCount() { return citationCount; }
    public void setCitationCount(int citationCount) { this.citationCount = citationCount; }

    public double getJournalImpactFactor() { return journalImpactFactor; }
    public void setJournalImpactFactor(double journalImpactFactor) { this.journalImpactFactor = journalImpactFactor; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }
}
