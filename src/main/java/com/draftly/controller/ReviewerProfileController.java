package com.draftly.controller;

import com.draftly.model.ReviewerProfile;
import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.ReviewerProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for reviewer profile management.
 */
@RestController
@RequestMapping("/api/reviewer-profiles")
public class ReviewerProfileController {

    private final ReviewerProfileService reviewerProfileService;

    public ReviewerProfileController(ReviewerProfileService reviewerProfileService) {
        this.reviewerProfileService = reviewerProfileService;
    }

    @PostMapping
    @RequireRoles({UserRole.ADMIN})
    public ReviewerProfile createProfile(@RequestBody ReviewerProfile reviewerProfile) {
        return reviewerProfileService.createProfile(reviewerProfile);
    }

    @GetMapping
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public List<ReviewerProfile> listProfiles() {
        return reviewerProfileService.getAllProfiles();
    }

    @GetMapping("/{id}")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<ReviewerProfile> getById(@PathVariable String id) {
        return reviewerProfileService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<ReviewerProfile> getByUserId(@PathVariable String userId) {
        return reviewerProfileService.getByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/domain/{domain}")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public List<ReviewerProfile> byDomain(@PathVariable String domain) {
        return reviewerProfileService.findByDomain(domain);
    }

    @PostMapping("/assign-submission")
    @RequireRoles({UserRole.ADMIN})
    public ReviewerProfile assignSubmission(@RequestBody Map<String, String> body) {
        return reviewerProfileService.assignSubmission(
                body.get("reviewerUserId"),
                body.get("submissionId")
        );
    }

    @PostMapping("/mark-completed")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public ReviewerProfile markCompleted(@RequestBody Map<String, String> body) {
        return reviewerProfileService.markReviewCompleted(body.get("reviewerUserId"));
    }

    @DeleteMapping("/{id}")
    @RequireRoles({UserRole.ADMIN})
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        reviewerProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
