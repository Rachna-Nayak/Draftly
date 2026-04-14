package com.draftly.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.draftly.model.Review;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findBySubmissionId(String submissionId);

    List<Review> findByReviewerId(String reviewerId);

    // Find all reviews for a submission, ordered by review date
    List<Review> findBySubmissionIdOrderByReviewDateDesc(String submissionId);

    // Find all reviews by a reviewer, ordered by review date
    List<Review> findByReviewerIdOrderByReviewDateDesc(String reviewerId);

    // Check if a review exists for a specific submission and reviewer combination
    boolean existsBySubmissionIdAndReviewerId(String submissionId, String reviewerId);

    // Find a specific review by submission and reviewer
    Review findBySubmissionIdAndReviewerId(String submissionId, String reviewerId);
}
