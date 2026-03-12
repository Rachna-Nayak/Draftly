package com.draftly.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a plagiarism check. (UC7)
 */
public class PlagiarismReport {

    private String paperId;
    private List<FlaggedSection> flaggedSections = new ArrayList<>();
    private double overallSimilarityScore;

    public PlagiarismReport() {}

    public PlagiarismReport(String paperId) {
        this.paperId = paperId;
    }

    // --- Getters and Setters ---

    public String getPaperId() { return paperId; }
    public void setPaperId(String paperId) { this.paperId = paperId; }

    public List<FlaggedSection> getFlaggedSections() { return flaggedSections; }
    public void setFlaggedSections(List<FlaggedSection> flaggedSections) { this.flaggedSections = flaggedSections; }

    public double getOverallSimilarityScore() { return overallSimilarityScore; }
    public void setOverallSimilarityScore(double overallSimilarityScore) { this.overallSimilarityScore = overallSimilarityScore; }

    public void addFlaggedSection(FlaggedSection section) {
        this.flaggedSections.add(section);
    }

    /**
     * Represents a section flagged for potential plagiarism.
     */
    public static class FlaggedSection {
        private String sectionName;
        private String matchedPaperId;
        private String matchedPaperTitle;
        private double similarityScore;

        public FlaggedSection() {}

        public FlaggedSection(String sectionName, String matchedPaperId, String matchedPaperTitle, double similarityScore) {
            this.sectionName = sectionName;
            this.matchedPaperId = matchedPaperId;
            this.matchedPaperTitle = matchedPaperTitle;
            this.similarityScore = similarityScore;
        }

        public String getSectionName() { return sectionName; }
        public void setSectionName(String sectionName) { this.sectionName = sectionName; }

        public String getMatchedPaperId() { return matchedPaperId; }
        public void setMatchedPaperId(String matchedPaperId) { this.matchedPaperId = matchedPaperId; }

        public String getMatchedPaperTitle() { return matchedPaperTitle; }
        public void setMatchedPaperTitle(String matchedPaperTitle) { this.matchedPaperTitle = matchedPaperTitle; }

        public double getSimilarityScore() { return similarityScore; }
        public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
    }
}
