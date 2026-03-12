package com.draftly.controller;

import com.draftly.model.ResearchPaper;
import com.draftly.service.FeedbackService;
import com.draftly.service.ResearchPaperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Paper writing (sections) and Faculty Feedback. (UC6)
 */
@RestController
@RequestMapping("/api/papers")
public class PaperController {

    private final ResearchPaperService researchPaperService;
    private final FeedbackService feedbackService;

    public PaperController(ResearchPaperService researchPaperService, FeedbackService feedbackService) {
        this.researchPaperService = researchPaperService;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/project/{projectId}")
    public List<ResearchPaper> listPapers(@PathVariable String projectId) {
        return researchPaperService.getPapersByProject(projectId);
    }

    @PostMapping
    public ResearchPaper createPaper(@RequestBody Map<String, String> body) {
        return researchPaperService.createPaper(body.get("projectId"), body.get("title"));
    }

    @GetMapping("/{paperId}")
    public ResponseEntity<ResearchPaper> getPaper(@PathVariable String paperId) {
        return researchPaperService.getPaperById(paperId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{paperId}/sections")
    public ResearchPaper addSection(@PathVariable String paperId, @RequestBody Map<String, Object> body) {
        return researchPaperService.addSection(
            paperId,
            (String) body.get("sectionName"),
            (String) body.get("content"),
            (int) body.get("order")
        );
    }

    @PostMapping("/{paperId}/feedback")
    public ResearchPaper submitFeedback(@PathVariable String paperId, @RequestBody Map<String, String> body) {
        return feedbackService.submitFeedback(
            paperId,
            body.get("facultyId"),
            body.get("facultyName"),
            body.get("sectionName"),
            body.get("comment"),
            body.get("status")
        );
    }

    @PostMapping("/{paperId}/approve")
    public ResearchPaper approvePaper(@PathVariable String paperId) {
        return feedbackService.approvePaper(paperId);
    }

    @DeleteMapping("/{paperId}")
    public ResponseEntity<Void> deletePaper(@PathVariable String paperId) {
        researchPaperService.deletePaper(paperId);
        return ResponseEntity.noContent().build();
    }
}
