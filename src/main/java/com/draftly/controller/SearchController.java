package com.draftly.controller;

import com.draftly.model.CredibilityReport;
import com.draftly.model.Paper;
import com.draftly.service.CredibilityService;
import com.draftly.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Controller for Literature Search (UC2) and Credibility Evaluation (UC3).
 */
@Controller
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
    private final CredibilityService credibilityService;

    public SearchController(SearchService searchService, CredibilityService credibilityService) {
        this.searchService = searchService;
        this.credibilityService = credibilityService;
    }

    @GetMapping
    public String showSearchPage(Model model) {
        model.addAttribute("papers", List.of());
        return "search/index";
    }

    @PostMapping
    public String searchPapers(@RequestParam String keywords,
                               @RequestParam(required = false) String domain,
                               @RequestParam(required = false) Integer startYear,
                               @RequestParam(required = false) Integer endYear,
                               @RequestParam(required = false) Integer minCitations,
                               Model model) {
        List<String> keywordList = Arrays.asList(keywords.split("\\s*,\\s*"));
        List<Paper> results = searchService.searchWithFilters(keywordList, domain, startYear, endYear, minCitations);
        model.addAttribute("papers", results);
        model.addAttribute("keywords", keywords);
        return "search/index";
    }

    @GetMapping("/credibility/{paperId}")
    public String evaluateCredibility(@PathVariable String paperId, Model model) {
        searchService.getPaperById(paperId).ifPresent(paper -> {
            CredibilityReport report = credibilityService.computeCredibility(paper);
            model.addAttribute("paper", paper);
            model.addAttribute("report", report);
        });
        return "search/credibility";
    }
}
