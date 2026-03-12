package com.draftly.service;

import com.draftly.model.Paper;
import com.draftly.model.PaperSection;
import com.draftly.model.PlagiarismReport;
import com.draftly.model.ResearchPaper;
import com.draftly.repository.PaperRepository;
import com.draftly.repository.ResearchPaperRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Plagiarism Service - Detects potential plagiarism and citation issues. (UC7)
 */
@Service
public class PlagiarismService {

    private static final double SIMILARITY_THRESHOLD = 0.7;

    private final PaperRepository paperRepository;
    private final ResearchPaperRepository researchPaperRepository;

    public PlagiarismService(PaperRepository paperRepository, ResearchPaperRepository researchPaperRepository) {
        this.paperRepository = paperRepository;
        this.researchPaperRepository = researchPaperRepository;
    }

    /**
     * UC7: Run plagiarism check on an entire research paper against the paper database.
     */
    public PlagiarismReport checkPlagiarism(String researchPaperId) {
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(researchPaperId);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("Research paper not found: " + researchPaperId);
        }

        ResearchPaper researchPaper = paperOpt.get();
        List<Paper> existingPapers = paperRepository.findAll();
        PlagiarismReport report = new PlagiarismReport(researchPaperId);

        double totalSimilarity = 0;
        int checks = 0;

        for (PaperSection section : researchPaper.getSections()) {
            for (Paper existing : existingPapers) {
                double similarity = computeSimilarity(section.getContent(), existing.getAbstractText());
                if (similarity > SIMILARITY_THRESHOLD) {
                    report.addFlaggedSection(new PlagiarismReport.FlaggedSection(
                            section.getSectionName(), existing.getId(),
                            existing.getTitle(), similarity
                    ));
                }
                totalSimilarity += similarity;
                checks++;
            }
        }

        report.setOverallSimilarityScore(checks > 0 ? totalSimilarity / checks : 0);
        return report;
    }

    /**
     * UC7: Compute cosine similarity between two text blocks.
     * TODO: Replace with proper TF-IDF + Cosine similarity.
     */
    public double computeSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isBlank() || text2.isBlank()) {
            return 0.0;
        }
        // TODO: Implement TF-IDF vectors + Cosine similarity using Apache Commons Text
        // Placeholder: Jaccard similarity on word sets
        var words1 = java.util.Set.of(text1.toLowerCase().split("\\W+"));
        var words2 = java.util.Set.of(text2.toLowerCase().split("\\W+"));
        long intersection = words1.stream().filter(words2::contains).count();
        long union = words1.size() + words2.size() - intersection;
        return union > 0 ? (double) intersection / union : 0.0;
    }
}
