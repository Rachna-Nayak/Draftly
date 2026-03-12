package com.draftly.controller;

import com.draftly.model.PlagiarismReport;
import com.draftly.service.PlagiarismService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Plagiarism & Integrity Risk Analysis. (UC7)
 */
@Controller
@RequestMapping("/plagiarism")
public class PlagiarismController {

    private final PlagiarismService plagiarismService;

    public PlagiarismController(PlagiarismService plagiarismService) {
        this.plagiarismService = plagiarismService;
    }

    @GetMapping("/{paperId}")
    public String checkPlagiarism(@PathVariable String paperId, Model model) {
        PlagiarismReport report = plagiarismService.checkPlagiarism(paperId);
        model.addAttribute("report", report);
        model.addAttribute("paperId", paperId);
        return "plagiarism/report";
    }
}
