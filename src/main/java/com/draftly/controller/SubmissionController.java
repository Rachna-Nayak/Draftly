package com.draftly.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.draftly.model.PaperVersion;
import com.draftly.model.PublishedPaper;
import com.draftly.model.Review;
import com.draftly.model.Submission;
import com.draftly.model.SubmissionFeedback;
import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.SubmissionService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public Submission createSubmission(@RequestBody Map<String, String> body) {
        return submissionService.createSubmission(
                body.get("projectId"),
                body.get("authorId"),
                body.get("title"),
                body.get("conferenceName"),
                body.get("track"),
                body.get("docxPath")
        );
    }

    @GetMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<Submission> listSubmissions(@RequestParam(required = false) String authorId,
                                            @RequestParam(required = false) String projectId,
                                            HttpServletRequest request) {
        if (authorId != null && !authorId.isBlank()) {
            return submissionService.getSubmissionsByAuthor(authorId);
        }
        if (projectId != null && !projectId.isBlank()) {
            return submissionService.getSubmissionsByProject(projectId);
        }

        UserRole currentRole = (UserRole) request.getAttribute("currentUserRole");
        String currentUserId = (String) request.getAttribute("currentUserId");

        if (currentRole != null && currentRole.matches(UserRole.ADMIN)) {
            return submissionService.getAllSubmissions();
        }

        if (currentUserId != null && !currentUserId.isBlank()) {
            return submissionService.getSubmissionsByAuthor(currentUserId);
        }

        throw new IllegalArgumentException("Unable to determine submissions scope for current user");
    }

    @GetMapping("/{submissionId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public Submission getSubmission(@PathVariable String submissionId) {
        return submissionService.getSubmissionById(submissionId);
    }

    @PostMapping("/{submissionId}/versions")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public PaperVersion uploadVersion(@PathVariable String submissionId, @RequestBody Map<String, String> body) {
        return submissionService.uploadPaperVersion(submissionId, body.get("filePath"), body.get("notes"));
    }

    @GetMapping("/{submissionId}/versions")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<PaperVersion> listVersions(@PathVariable String submissionId) {
        return submissionService.getVersions(submissionId);
    }

    @PostMapping("/{submissionId}/references")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public Submission attachReference(@PathVariable String submissionId, @RequestBody Map<String, String> body) {
        return submissionService.attachReference(submissionId, body.get("referenceId"));
    }

    @PostMapping("/{submissionId}/submit")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public Submission submitPaper(@PathVariable String submissionId) {
        return submissionService.submitPaper(submissionId);
    }

    @PostMapping("/{submissionId}/reviews")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public Review addReview(@PathVariable String submissionId, @RequestBody Map<String, Object> body) {
        int rating = body.get("rating") == null ? 0 : ((Number) body.get("rating")).intValue();
        return submissionService.addReview(
                submissionId,
                (String) body.get("reviewerId"),
                rating,
                (String) body.get("comments"),
                Review.Decision.valueOf(((String) body.get("decision")).toUpperCase())
        );
    }

    @GetMapping("/{submissionId}/reviews")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<Review> listReviews(@PathVariable String submissionId) {
        return submissionService.getReviews(submissionId);
    }

    @PostMapping("/{submissionId}/feedback")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public SubmissionFeedback storeFeedback(@PathVariable String submissionId, @RequestBody Map<String, String> body) {
        return submissionService.storeFeedback(submissionId, body.get("reviewerId"), body.get("message"));
    }

    @GetMapping("/{submissionId}/feedback")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<SubmissionFeedback> listFeedback(@PathVariable String submissionId) {
        return submissionService.getFeedback(submissionId);
    }

    @PostMapping("/{submissionId}/publish")
    @RequireRoles({UserRole.ADMIN})
    public PublishedPaper publishSubmission(@PathVariable String submissionId, @RequestBody Map<String, Object> body) {
        int publicationYear = body.get("publicationYear") == null ? 0 : ((Number) body.get("publicationYear")).intValue();
        return submissionService.publishSubmission(
                submissionId,
                (String) body.get("doi"),
                (String) body.get("journal"),
                publicationYear
        );
    }

    @GetMapping("/{submissionId}/published")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public PublishedPaper getPublishedPaper(@PathVariable String submissionId) {
        return submissionService.getPublishedPaper(submissionId);
    }
}
