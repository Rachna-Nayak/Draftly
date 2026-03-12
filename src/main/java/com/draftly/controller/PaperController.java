package com.draftly.controller;

import com.draftly.model.ResearchPaper;
import com.draftly.service.FeedbackService;
import com.draftly.service.ResearchPaperService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Paper writing (sections) and Faculty Feedback. (UC6)
 */
@Controller
@RequestMapping("/papers")
public class PaperController {

    private final ResearchPaperService researchPaperService;
    private final FeedbackService feedbackService;

    public PaperController(ResearchPaperService researchPaperService, FeedbackService feedbackService) {
        this.researchPaperService = researchPaperService;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/project/{projectId}")
    public String listPapers(@PathVariable String projectId, Model model) {
        List<ResearchPaper> papers = researchPaperService.getPapersByProject(projectId);
        model.addAttribute("papers", papers);
        model.addAttribute("projectId", projectId);
        return "paper/list";
    }

    @GetMapping("/new/{projectId}")
    public String showCreateForm(@PathVariable String projectId, Model model) {
        model.addAttribute("projectId", projectId);
        return "paper/create";
    }

    @PostMapping("/new")
    public String createPaper(@RequestParam String projectId, @RequestParam String title) {
        researchPaperService.createPaper(projectId, title);
        return "redirect:/papers/project/" + projectId;
    }

    @GetMapping("/{paperId}")
    public String viewPaper(@PathVariable String paperId, Model model) {
        researchPaperService.getPaperById(paperId).ifPresent(p -> model.addAttribute("paper", p));
        return "paper/view";
    }

    @PostMapping("/{paperId}/sections")
    public String addSection(@PathVariable String paperId,
                             @RequestParam String sectionName,
                             @RequestParam String content,
                             @RequestParam int order) {
        researchPaperService.addSection(paperId, sectionName, content, order);
        return "redirect:/papers/" + paperId;
    }

    @PostMapping("/{paperId}/feedback")
    public String submitFeedback(@PathVariable String paperId,
                                 @RequestParam String facultyId,
                                 @RequestParam String facultyName,
                                 @RequestParam String sectionName,
                                 @RequestParam String comment,
                                 @RequestParam String status) {
        feedbackService.submitFeedback(paperId, facultyId, facultyName, sectionName, comment, status);
        return "redirect:/papers/" + paperId;
    }

    @PostMapping("/{paperId}/approve")
    public String approvePaper(@PathVariable String paperId) {
        feedbackService.approvePaper(paperId);
        return "redirect:/papers/" + paperId;
    }
}
