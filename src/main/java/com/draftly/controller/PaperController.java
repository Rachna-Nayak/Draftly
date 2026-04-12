package com.draftly.controller;

import com.draftly.model.ResearchPaper;
import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.FeedbackService;
import com.draftly.service.ResearchPaperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<ResearchPaper> listPapers(@PathVariable String projectId) {
        return researchPaperService.getPapersByProject(projectId);
    }

    @PostMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public ResearchPaper createPaper(@RequestBody Map<String, String> body) {
        return researchPaperService.createPaper(body.get("projectId"), body.get("title"));
    }

    /**
     * FR5 + FR6: Upload a DOCX file and parse it into a ResearchPaper with sections.
     * Accepts multipart form data with the file, projectId, title, and optional template (IEEE or LNCS).
     */
    @PostMapping("/upload")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public ResponseEntity<?> uploadDocx(
            @RequestParam("file") MultipartFile file,
            @RequestParam String projectId,
            @RequestParam String title,
            @RequestParam(defaultValue = "IEEE") String template,
            @RequestParam(required = false) String referencesBibtex,
            @RequestParam(value = "referencesFile", required = false) MultipartFile referencesFile) {

        // FR5: Validate file format and size
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".docx")) {
            return ResponseEntity.badRequest().body("Invalid file format. Only .docx files are accepted.");
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Uploaded file is empty.");
        }
        // 20 MB limit
        if (file.getSize() > 20 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("File exceeds the maximum allowed size of 20 MB.");
        }

        try {
            String uploadedReferences = referencesBibtex;
            if (referencesFile != null && !referencesFile.isEmpty()) {
                uploadedReferences = new String(referencesFile.getBytes(), StandardCharsets.UTF_8);
            }

            ResearchPaper paper = researchPaperService.importFromDocx(
                    projectId,
                    title,
                    file.getBytes(),
                    template,
                    uploadedReferences
            );
            return ResponseEntity.ok(paper);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to read the uploaded file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/{paperId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<ResearchPaper> getPaper(@PathVariable String paperId) {
        return researchPaperService.getPaperById(paperId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{paperId}/sections")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public ResearchPaper addSection(@PathVariable String paperId, @RequestBody Map<String, Object> body) {
        return researchPaperService.addSection(
            paperId,
            (String) body.get("sectionName"),
            (String) body.get("content"),
            (int) body.get("order")
        );
    }

    @PostMapping("/{paperId}/feedback")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
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
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public ResearchPaper approvePaper(@PathVariable String paperId) {
        return feedbackService.approvePaper(paperId);
    }

    @DeleteMapping("/{paperId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public ResponseEntity<Void> deletePaper(@PathVariable String paperId) {
        researchPaperService.deletePaper(paperId);
        return ResponseEntity.noContent().build();
    }
}
