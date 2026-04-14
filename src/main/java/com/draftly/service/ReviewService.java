package com.draftly.service;

import com.draftly.dto.ReviewSubmissionRequest;
import com.draftly.model.Review;
import com.draftly.model.ReviewSchedule;
import com.draftly.model.Submission;
import com.draftly.repository.ReviewRepository;
import com.draftly.repository.ReviewScheduleRepository;
import com.draftly.repository.SubmissionRepository;
import com.draftly.util.ReviewConsensusCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final SubmissionRepository submissionRepository;
    private final ReviewerProfileService reviewerProfileService;
    private final NotificationService notificationService;

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewScheduleRepository reviewScheduleRepository,
                         SubmissionRepository submissionRepository,
                         ReviewerProfileService reviewerProfileService,
                         NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.reviewScheduleRepository = reviewScheduleRepository;
        this.submissionRepository = submissionRepository;
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

    /**
     * Get all reviews for a submission, ordered by review date
     */
    public List<Review> getBySubmissionIdOrderedByDate(String submissionId) {
        return reviewRepository.findBySubmissionIdOrderByReviewDateDesc(submissionId);
    }

    /**
     * Get all reviews by a reviewer, ordered by review date
     */
    public List<Review> getByReviewerIdOrderedByDate(String reviewerId) {
        return reviewRepository.findByReviewerIdOrderByReviewDateDesc(reviewerId);
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

    /**
     * Validate review submission before accepting it.
     * Checks for:
     * - Duplicate reviews
     * - Deadline compliance
     * - Reviewer assignment
     * - Valid submission status
     *
     * @param request The review submission request
     * @return Validation result with error messages if any
     */
    public ValidationResult validateReviewSubmission(ReviewSubmissionRequest request) {
        // Check for duplicate review
        if (reviewRepository.existsBySubmissionIdAndReviewerId(
                request.getSubmissionId(),
                request.getReviewerId())) {
            return ValidationResult.failure("You have already submitted a review for this submission");
        }

        // Check if submission exists
        Optional<Submission> submissionOpt = submissionRepository.findById(request.getSubmissionId());
        if (submissionOpt.isEmpty()) {
            return ValidationResult.failure("Submission not found");
        }

        Submission submission = submissionOpt.get();

        // Check submission status
        if (submission.getStatus() != Submission.Status.SUBMITTED &&
            submission.getStatus() != Submission.Status.UNDER_REVIEW &&
            submission.getStatus() != Submission.Status.REVISION_REQUIRED) {
            return ValidationResult.failure("Submission is not in a reviewable state");
        }

        // Check if reviewer is assigned to this submission
        List<ReviewSchedule> schedules = reviewScheduleRepository.findBySubmissionId(request.getSubmissionId());
        boolean reviewerAssigned = schedules.stream()
                .anyMatch(s -> s.getReviewerUserId().equals(request.getReviewerId()));

        if (!reviewerAssigned) {
            return ValidationResult.failure("You are not assigned to review this submission");
        }

        // Check deadline
        for (ReviewSchedule schedule : schedules) {
            if (schedule.getReviewerUserId().equals(request.getReviewerId())) {
                if (schedule.getDueAt() != null && schedule.getDueAt().isBefore(LocalDateTime.now())) {
                    return ValidationResult.failure("Review deadline has passed");
                }
                break;
            }
        }

        // Validate comment length
        if (request.getComments() == null || request.getComments().length() < 50) {
            return ValidationResult.failure("Comments must be at least 50 characters");
        }

        return ValidationResult.success();
    }

    /**
     * Submit a review with full validation
     */
    public Review submitReviewWithValidation(ReviewSubmissionRequest request) {
        // Validate
        ValidationResult validation = validateReviewSubmission(request);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getErrorMessage());
        }

        // Create review object
        Review review = new Review();
        review.setSubmissionId(request.getSubmissionId());
        review.setReviewerId(request.getReviewerId());
        review.setDecision(request.getReviewDecision());
        review.setComments(request.getComments());
        review.setRating(mapDecisionToRating(request.getReviewDecision()));
        review.setConfidence(request.getConfidence());

        // Set optional category ratings
        if (request.getTechnicalQuality() != null) {
            review.setTechnicalQuality(request.getTechnicalQuality());
        }
        if (request.getClarity() != null) {
            review.setClarity(request.getClarity());
        }
        if (request.getOriginality() != null) {
            review.setOriginality(request.getOriginality());
        }
        if (request.getSignificance() != null) {
            review.setSignificance(request.getSignificance());
        }

        // Save review
        Review savedReview = reviewRepository.save(review);

        // Mark the review schedule completed for this reviewer/submission
        List<ReviewSchedule> schedules = reviewScheduleRepository.findBySubmissionId(request.getSubmissionId());
        for (ReviewSchedule schedule : schedules) {
            if (request.getReviewerId().equals(schedule.getReviewerUserId())
                    && schedule.getStatus() != ReviewSchedule.Status.COMPLETED) {
                schedule.setStatus(ReviewSchedule.Status.COMPLETED);
                reviewScheduleRepository.save(schedule);
                break;
            }
        }

        // Recompute submission status based on consensus when all assigned reviews are submitted
        Submission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        List<Review> allReviews = reviewRepository.findBySubmissionId(request.getSubmissionId());
        int totalReviewersAssigned = schedules.size();

        if (submission.getStatus() == Submission.Status.SUBMITTED) {
            submission.setStatus(Submission.Status.UNDER_REVIEW);
        }

        if (totalReviewersAssigned > 0) {
            Submission.Status consensusStatus = ReviewConsensusCalculator
                    .calculateConsensusStatus(allReviews, totalReviewersAssigned);

            if (allReviews.size() >= totalReviewersAssigned) {
                submission.setStatus(consensusStatus);
                submission.setLocked(consensusStatus != Submission.Status.REVISION_REQUIRED);
            }
        }

        submission.setUpdatedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        // Update reviewer profile
        reviewerProfileService.markReviewCompleted(request.getReviewerId());

        return savedReview;
    }

    /**
     * Map review decision to a numeric rating for backward compatibility
     */
    private int mapDecisionToRating(Review.Decision decision) {
        switch (decision) {
            case STRONG_ACCEPT:
            case ACCEPT:
                return 5;
            case BORDERLINE:
                return 3;
            case REJECT:
                return 2;
            case STRONG_REJECT:
                return 1;
            default:
                return 3;
        }
    }

    /**
     * Get pending reviews for a reviewer with details
     */
    public List<PendingReviewDTO> getPendingReviewsForReviewer(String reviewerId) {
        List<ReviewSchedule> reviewerSchedules = reviewScheduleRepository.findByReviewerUserId(reviewerId);

        return reviewerSchedules.stream()
                .map(schedule -> {
                    Optional<Submission> submissionOpt = submissionRepository.findById(schedule.getSubmissionId());
                    if (submissionOpt.isPresent()) {
                        Submission submission = submissionOpt.get();
                Review existingReview = reviewRepository.findBySubmissionIdAndReviewerId(
                    schedule.getSubmissionId(),
                    reviewerId);
                boolean completed = existingReview != null;

                        return new PendingReviewDTO(
                                schedule.getSubmissionId(),
                                submission.getTitle(),
                                submission.getConferenceName(),
                                submission.getTrack(),
                                schedule.getDueAt(),
                                calculateDaysUntilDue(schedule.getDueAt()),
                    !completed,
                    completed,
                    completed ? existingReview.getReviewDate() : null
                        );
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .toList();
    }

    private long calculateDaysUntilDue(LocalDateTime dueDate) {
        if (dueDate == null) return -1;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
    }

    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }

    /**
     * Helper class for validation results
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * DTO for pending reviews
     */
    public static class PendingReviewDTO {
        private final String submissionId;
        private final String title;
        private final String conference;
        private final String track;
        private final LocalDateTime dueDate;
        private final long daysUntilDue;
        private final boolean notYetReviewed;
        private final boolean completed;
        private final LocalDateTime completedAt;

        public PendingReviewDTO(String submissionId, String title, String conference, String track,
                              LocalDateTime dueDate, long daysUntilDue, boolean notYetReviewed,
                              boolean completed, LocalDateTime completedAt) {
            this.submissionId = submissionId;
            this.title = title;
            this.conference = conference;
            this.track = track;
            this.dueDate = dueDate;
            this.daysUntilDue = daysUntilDue;
            this.notYetReviewed = notYetReviewed;
            this.completed = completed;
            this.completedAt = completedAt;
        }

        // Getters
        public String getSubmissionId() { return submissionId; }
        public String getTitle() { return title; }
        public String getConference() { return conference; }
        public String getTrack() { return track; }
        public LocalDateTime getDueDate() { return dueDate; }
        public long getDaysUntilDue() { return daysUntilDue; }
        public boolean isNotYetReviewed() { return notYetReviewed; }
        public boolean isCompleted() { return completed; }
        public LocalDateTime getCompletedAt() { return completedAt; }
    }
}