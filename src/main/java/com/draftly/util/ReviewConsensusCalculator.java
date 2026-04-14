package com.draftly.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.draftly.model.Review;
import com.draftly.model.Submission;

/**
 * Utility class to calculate consensus status based on multiple reviewer decisions.
 * Implements strict consensus logic where all reviewers must agree on a decision.
 */
public class ReviewConsensusCalculator {

    private ReviewConsensusCalculator() {
        // Utility class
    }

    /**
     * Calculate the final submission status based on consensus from multiple reviews.
     *
     * Consensus Rules (Strict):
     * - If all reviews = STRONG_ACCEPT → ACCEPTED
     * - If all reviews = REJECT or STRONG_REJECT → REJECTED
     * - If all reviews = ACCEPT → ACCEPTED
     * - If any mixed decisions (different decisions) → REVISION_REQUIRED
     * - If reviews < totalReviewersAssigned → UNDER_REVIEW (waiting for more reviews)
     *
     * @param reviews List of all reviews for the submission
     * @param totalReviewersAssigned Total number of reviewers assigned
     * @return The calculated Submission.Status
     */
    public static Submission.Status calculateConsensusStatus(List<Review> reviews, int totalReviewersAssigned) {
        if (reviews == null || reviews.isEmpty()) {
            return Submission.Status.SUBMITTED;
        }

        // If not all reviewers have submitted, keep status as UNDER_REVIEW
        if (reviews.size() < totalReviewersAssigned) {
            return Submission.Status.UNDER_REVIEW;
        }

        // Count decisions
        Map<Review.Decision, Long> decisionCounts = reviews.stream()
                .map(Review::getDecision)
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        // If all decisions are the same
        if (decisionCounts.size() == 1) {
            Review.Decision decision = decisionCounts.keySet().iterator().next();
            return mapDecisionToStatus(decision);
        }

        // Mixed decisions: Apply consensus rules
        boolean hasAccept = decisionCounts.containsKey(Review.Decision.ACCEPT)
                || decisionCounts.containsKey(Review.Decision.STRONG_ACCEPT);
        boolean hasReject = decisionCounts.containsKey(Review.Decision.REJECT)
                || decisionCounts.containsKey(Review.Decision.STRONG_REJECT);
        boolean hasBorderline = decisionCounts.containsKey(Review.Decision.BORDERLINE);

        // If all are accepts (ACCEPT or STRONG_ACCEPT)
        if (!hasReject && !hasBorderline) {
            return Submission.Status.ACCEPTED;
        }

        // If all are rejects (REJECT or STRONG_REJECT)
        if (!hasAccept && !hasBorderline) {
            return Submission.Status.REJECTED;
        }

        // Any mixed decision with borderline or conflicting accepts/rejects → REVISION_REQUIRED
        return Submission.Status.REVISION_REQUIRED;
    }

    /**
     * Map a single reviewer decision to a submission status.
     * Used for single reviewer scenarios or to determine unanimous decisions.
     *
     * @param decision The review decision
     * @return The corresponding submission status
     */
    private static Submission.Status mapDecisionToStatus(Review.Decision decision) {
        switch (decision) {
            case ACCEPT:
            case STRONG_ACCEPT:
                return Submission.Status.ACCEPTED;
            case REJECT:
            case STRONG_REJECT:
                return Submission.Status.REJECTED;
            case REVISION:
            case BORDERLINE:
                return Submission.Status.REVISION_REQUIRED;
            default:
                return Submission.Status.UNDER_REVIEW;
        }
    }

    /**
     * Get a summary of reviewer decisions for display purposes.
     *
     * @param reviews List of all reviews
     * @return A formatted string summary of decisions
     */
    public static String getDecisionSummary(List<Review> reviews) {
        Map<Review.Decision, Long> counts = reviews.stream()
                .map(Review::getDecision)
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
    }

    /**
     * Calculate average rating from all reviews.
     *
     * @param reviews List of all reviews
     * @return Average rating (0.0 if no reviews)
     */
    public static double calculateAverageRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
