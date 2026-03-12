package com.draftly.controller;

import com.draftly.model.PlagiarismReport;
import com.draftly.service.PlagiarismService;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Plagiarism & Integrity Risk Analysis. (UC7)
 */
@RestController
@RequestMapping("/api/plagiarism")
public class PlagiarismController {

    private final PlagiarismService plagiarismService;

    public PlagiarismController(PlagiarismService plagiarismService) {
        this.plagiarismService = plagiarismService;
    }

    @GetMapping("/{paperId}")
    public PlagiarismReport checkPlagiarism(@PathVariable String paperId) {
        return plagiarismService.checkPlagiarism(paperId);
    }
}
