package com.draftly.model;

/**
 * Represents the credibility evaluation result for a paper. (UC3)
 */
public class CredibilityReport {

    private String paperId;
    private String paperTitle;
    private double citationScore;
    private double journalScore;
    private double recencyScore;
    private double overallScore;
    private String suggestedCitation;

    public CredibilityReport() {}

    public CredibilityReport(String paperId, String paperTitle, double citationScore,
                             double journalScore, double recencyScore) {
        this.paperId = paperId;
        this.paperTitle = paperTitle;
        this.citationScore = citationScore;
        this.journalScore = journalScore;
        this.recencyScore = recencyScore;
        this.overallScore = 0.5 * citationScore + 0.3 * journalScore + 0.2 * recencyScore;
    }

    // --- Getters and Setters ---

    public String getPaperId() { return paperId; }
    public void setPaperId(String paperId) { this.paperId = paperId; }

    public String getPaperTitle() { return paperTitle; }
    public void setPaperTitle(String paperTitle) { this.paperTitle = paperTitle; }

    public double getCitationScore() { return citationScore; }
    public void setCitationScore(double citationScore) { this.citationScore = citationScore; }

    public double getJournalScore() { return journalScore; }
    public void setJournalScore(double journalScore) { this.journalScore = journalScore; }

    public double getRecencyScore() { return recencyScore; }
    public void setRecencyScore(double recencyScore) { this.recencyScore = recencyScore; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public String getSuggestedCitation() { return suggestedCitation; }
    public void setSuggestedCitation(String suggestedCitation) { this.suggestedCitation = suggestedCitation; }
}
