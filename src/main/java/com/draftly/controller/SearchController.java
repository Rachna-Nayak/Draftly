package com.draftly.controller;

import com.draftly.model.CredibilityReport;
import com.draftly.model.Paper;
import com.draftly.service.CredibilityService;
import com.draftly.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Literature Search (UC2) and Credibility Evaluation (UC3).
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;
    private final CredibilityService credibilityService;

    public SearchController(SearchService searchService, CredibilityService credibilityService) {
        this.searchService = searchService;
        this.credibilityService = credibilityService;
    }

    @GetMapping
    public List<Paper> searchPapers(@RequestParam String keywords,
                                    @RequestParam(required = false) String domain,
                                    @RequestParam(required = false) Integer startYear,
                                    @RequestParam(required = false) Integer endYear,
                                    @RequestParam(required = false) Integer minCitations) {
        List<String> keywordList = Arrays.asList(keywords.split("\\s*,\\s*"));
        return searchService.searchWithFilters(keywordList, domain, startYear, endYear, minCitations);
    }

    @GetMapping("/all")
    public List<Paper> getAllPapers() {
        return searchService.getAllPapers();
    }

    @GetMapping("/credibility/{paperId}")
    public ResponseEntity<Map<String, Object>> evaluateCredibility(@PathVariable String paperId) {
        return searchService.getPaperById(paperId)
                .map(paper -> {
                    CredibilityReport report = credibilityService.computeCredibility(paper);
                    return ResponseEntity.ok(Map.<String, Object>of("paper", paper, "report", report));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
