package com.draftly.service;

import com.draftly.model.CredibilityReport;
import com.draftly.model.Paper;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.stream.Collectors;

/**
 * Credibility Service - Evaluates source credibility and generates citations. (UC3)
 */
@Service
public class CredibilityService {

    private static final int CURRENT_YEAR = Year.now().getValue();

    /**
     * UC3: Compute credibility score for a paper.
     * Formula: 0.5 * citationScore + 0.3 * journalScore + 0.2 * recencyScore
     */
    public CredibilityReport computeCredibility(Paper paper) {
        double citationScore = normalizeCitationCount(paper.getCitationCount());
        double journalScore = normalizeJournalImpact(paper.getJournalImpactFactor());
        double recencyScore = computeRecencyScore(paper.getPublicationYear());

        CredibilityReport report = new CredibilityReport(
                paper.getId(), paper.getTitle(),
                citationScore, journalScore, recencyScore
        );

        // Generate a default APA citation
        report.setSuggestedCitation(generateCitationAPA(paper));
        return report;
    }

    /**
     * UC3/UC4: Generate APA format citation.
     */
    public String generateCitationAPA(Paper paper) {
        // Format: Author(s) (Year). Title. Journal. DOI
        String authors = String.join(", ", paper.getAuthors());
        return String.format("%s (%d). %s. %s. %s",
                authors, paper.getPublicationYear(), paper.getTitle(),
                paper.getJournal(), paper.getDoi() != null ? paper.getDoi() : "");
    }

    /**
     * UC4: Generate MLA format citation.
     */
    public String generateCitationMLA(Paper paper) {
        String authors = String.join(", ", paper.getAuthors());
        return String.format("%s. \"%s.\" %s, %d.",
                authors, paper.getTitle(), paper.getJournal(), paper.getPublicationYear());
    }

    /**
     * UC4: Generate IEEE format citation.
     */
    public String generateCitationIEEE(Paper paper) {
        String authors = paper.getAuthors().stream()
                .map(a -> {
                    String[] parts = a.split(" ");
                    if (parts.length >= 2) {
                        return parts[0].charAt(0) + ". " + parts[parts.length - 1];
                    }
                    return a;
                })
                .collect(Collectors.joining(", "));
        return String.format("%s, \"%s,\" %s, %d.",
                authors, paper.getTitle(), paper.getJournal(), paper.getPublicationYear());
    }

    // --- Normalization Helpers ---

    private double normalizeCitationCount(int citations) {
        // Normalize to 0-1 scale (assume max ~500 citations)
        return Math.min(citations / 500.0, 1.0);
    }

    private double normalizeJournalImpact(double impactFactor) {
        // Normalize to 0-1 scale (assume max ~10)
        return Math.min(impactFactor / 10.0, 1.0);
    }

    private double computeRecencyScore(int publicationYear) {
        int age = CURRENT_YEAR - publicationYear;
        // More recent = higher score. Papers older than 20 years get 0
        return Math.max(0, 1.0 - (age / 20.0));
    }
}
