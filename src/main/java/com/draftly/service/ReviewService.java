package com.draftly.service;

import com.draftly.model.Review;
import com.draftly.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewerProfileService reviewerProfileService;
    private final NotificationService notificationService;

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewerProfileService reviewerProfileService,
                         NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.reviewerProfileService = reviewerProfileService;
        this.notificationService = notificationService;
    }

    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getById(String id) {
        return reviewRepository.findById(id);
    }

    public List<Review> getBySubmissionId(String submissionId) {
        return reviewRepository.findBySubmissionId(submissionId);
    }

    public List<Review> getByReviewerUserId(String reviewerUserId) {
        return reviewRepository.findByReviewerId(reviewerUserId);
    }

    public Review saveReviewAndMarkCompleted(Review review, String adminUserId) {
        Review saved = createReview(review);

        if (saved.getReviewerId() != null && !saved.getReviewerId().isBlank()) {
            reviewerProfileService.markReviewCompleted(saved.getReviewerId());
            notificationService.notifyReviewCompleted(
                    saved.getReviewerId(),
                    adminUserId == null || adminUserId.isBlank() ? saved.getReviewerId() : adminUserId,
                    saved.getSubmissionId()
            );
        }

        return saved;
    }

    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }
}