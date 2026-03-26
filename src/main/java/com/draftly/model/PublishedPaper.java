package com.draftly.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "published_papers")
public class PublishedPaper {

    @Id
    private String id;
    private String submissionId;
    private String doi;
    private String journal;
    private int publicationYear;
    private LocalDateTime publishedAt;

    public PublishedPaper() {
        this.publishedAt = LocalDateTime.now();
    }

    public PublishedPaper(String submissionId, String doi, String journal, int publicationYear) {
        this();
        this.submissionId = submissionId;
        this.doi = doi;
        this.journal = journal;
        this.publicationYear = publicationYear;
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

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
