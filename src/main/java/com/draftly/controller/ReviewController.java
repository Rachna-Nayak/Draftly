package com.draftly.controller;

import com.draftly.dto.ReviewSubmissionRequest;
import com.draftly.dto.SubmissionReviewContext;
import com.draftly.model.Review;
import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.ReviewService;
import com.draftly.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final SubmissionService submissionService;

    public ReviewController(ReviewService reviewService, SubmissionService submissionService) {
        this.reviewService = reviewService;
        this.submissionService = submissionService;
    }

    @PostMapping
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @GetMapping
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public List<Review> listReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<Review> getById(@PathVariable String id) {
        return reviewService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/submission/{submissionId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<Review> getBySubmissionId(@PathVariable String submissionId) {
        return reviewService.getBySubmissionId(submissionId);
    }

    @GetMapping("/reviewer/{reviewerUserId}")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public List<Review> getByReviewerUserId(@PathVariable String reviewerUserId) {
        return reviewService.getByReviewerUserId(reviewerUserId);
    }

    /**
     * Get pending reviews for the current reviewer
     */
    @GetMapping("/pending/my-reviews")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<List<ReviewService.PendingReviewDTO>> getMyPendingReviews(
            @RequestHeader(name = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<ReviewService.PendingReviewDTO> pendingReviews = reviewService.getPendingReviewsForReviewer(userId);
        return ResponseEntity.ok(pendingReviews);
    }

    /**
     * Get submission context for review (with blind review applied - no author info)
     */
    @GetMapping("/submission/{submissionId}/context")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<?> getSubmissionReviewContext(
            @PathVariable String submissionId,
            @RequestHeader(name = "X-User-Id", required = false) String reviewerId) {
        if (reviewerId == null || reviewerId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reviewer ID required"));
        }

        try {
            SubmissionReviewContext context = submissionService.getSubmissionForReview(submissionId, reviewerId);
            return ResponseEntity.ok(context);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Submit a review with full validation
     */
    @PostMapping("/submit")
    @RequireRoles({UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<?> submitReview(
            @RequestBody ReviewSubmissionRequest request,
            @RequestHeader(name = "X-User-Id", required = false) String reviewerId) {
        if (reviewerId == null || reviewerId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reviewer ID required"));
        }

        // Enforce reviewer identity from trusted request header rather than client body.
        request.setReviewerId(reviewerId);

        try {
            Review savedReview = reviewService.submitReviewWithValidation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Review submission failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRoles({UserRole.ADMIN})
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}