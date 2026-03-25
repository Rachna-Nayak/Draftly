package com.draftly.controller;

import com.draftly.model.ReviewerProfile;
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
    public ReviewerProfile createProfile(@RequestBody ReviewerProfile reviewerProfile) {
        return reviewerProfileService.createProfile(reviewerProfile);
    }

    @GetMapping
    public List<ReviewerProfile> listProfiles() {
        return reviewerProfileService.getAllProfiles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewerProfile> getById(@PathVariable String id) {
        return reviewerProfileService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ReviewerProfile> getByUserId(@PathVariable String userId) {
        return reviewerProfileService.getByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/domain/{domain}")
    public List<ReviewerProfile> byDomain(@PathVariable String domain) {
        return reviewerProfileService.findByDomain(domain);
    }

    @PostMapping("/assign-submission")
    public ReviewerProfile assignSubmission(@RequestBody Map<String, String> body) {
        return reviewerProfileService.assignSubmission(
                body.get("reviewerUserId"),
                body.get("submissionId")
        );
    }

    @PostMapping("/mark-completed")
    public ReviewerProfile markCompleted(@RequestBody Map<String, String> body) {
        return reviewerProfileService.markReviewCompleted(body.get("reviewerUserId"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        reviewerProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
