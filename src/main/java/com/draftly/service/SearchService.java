package com.draftly.service;

import com.draftly.model.Paper;
import com.draftly.repository.PaperRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Search Service - Discovers and ranks relevant academic literature. (UC2)
 */
@Service
public class SearchService {

    private final PaperRepository paperRepository;

    public SearchService(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
    }

    /**
     * UC2: Search and rank papers by relevance to given keywords.
     * Uses TF-IDF + Cosine similarity for ranking.
     */
    public List<Paper> rankByRelevance(List<String> keywords) {
        List<Paper> allPapers = paperRepository.findAll();
        // TODO: Implement TF-IDF + Cosine similarity ranking
        // Placeholder: simple keyword overlap scoring
        return allPapers.stream()
                .sorted((p1, p2) -> {
                    int score1 = computeKeywordOverlap(keywords, p1);
                    int score2 = computeKeywordOverlap(keywords, p2);
                    return Integer.compare(score2, score1); // descending
                })
                .collect(Collectors.toList());
    }

    /**
     * UC2: Search with filters (year range, domain, minimum citations).
     */
    public List<Paper> searchWithFilters(List<String> keywords, String domain,
                                         Integer startYear, Integer endYear,
                                         Integer minCitations) {
        List<Paper> ranked = rankByRelevance(keywords);
        return ranked.stream()
                .filter(p -> domain == null || domain.isBlank() || p.getDomain().equalsIgnoreCase(domain))
                .filter(p -> startYear == null || p.getPublicationYear() >= startYear)
                .filter(p -> endYear == null || p.getPublicationYear() <= endYear)
                .filter(p -> minCitations == null || p.getCitationCount() >= minCitations)
                .collect(Collectors.toList());
    }

    /**
     * Simple keyword overlap score (placeholder for TF-IDF).
     */
    private int computeKeywordOverlap(List<String> queryKeywords, Paper paper) {
        Set<String> paperKeywords = new HashSet<>();
        paper.getKeywords().forEach(k -> paperKeywords.add(k.toLowerCase()));
        if (paper.getTitle() != null) {
            Arrays.stream(paper.getTitle().toLowerCase().split("\\W+"))
                    .forEach(paperKeywords::add);
        }
        return (int) queryKeywords.stream()
                .map(String::toLowerCase)
                .filter(paperKeywords::contains)
                .count();
    }

    /**
     * Get all papers in the database.
     */
    public List<Paper> getAllPapers() {
        return paperRepository.findAll();
    }

    /**
     * Get a paper by ID.
     */
    public Optional<Paper> getPaperById(String paperId) {
        return paperRepository.findById(paperId);
    }

    /**
     * Add a paper to the database.
     */
    public Paper addPaper(Paper paper) {
        return paperRepository.save(paper);
    }
}
