package com.draftly.controller;

import com.draftly.model.Review;
import com.draftly.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @GetMapping
    public List<Review> listReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable String id) {
        return reviewService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/submission/{submissionId}")
    public List<Review> getBySubmissionId(@PathVariable String submissionId) {
        return reviewService.getBySubmissionId(submissionId);
    }

    @GetMapping("/reviewer/{reviewerUserId}")
    public List<Review> getByReviewerUserId(@PathVariable String reviewerUserId) {
        return reviewService.getByReviewerUserId(reviewerUserId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}