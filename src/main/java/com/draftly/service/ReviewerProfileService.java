package com.draftly.service;

import com.draftly.model.ReviewerProfile;
import com.draftly.repository.ReviewerProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for reviewer profile management.
 */
@Service
public class ReviewerProfileService {

    private final ReviewerProfileRepository reviewerProfileRepository;

    public ReviewerProfileService(ReviewerProfileRepository reviewerProfileRepository) {
        this.reviewerProfileRepository = reviewerProfileRepository;
    }

    public ReviewerProfile createProfile(ReviewerProfile reviewerProfile) {
        return reviewerProfileRepository.save(reviewerProfile);
    }

    public Optional<ReviewerProfile> getById(String id) {
        return reviewerProfileRepository.findById(id);
    }

    public Optional<ReviewerProfile> getByUserId(String userId) {
        return reviewerProfileRepository.findByUserId(userId);
    }

    public List<ReviewerProfile> getAllProfiles() {
        return reviewerProfileRepository.findAll();
    }

    public List<ReviewerProfile> findByDomain(String domain) {
        return reviewerProfileRepository.findByExpertiseDomainsContaining(domain);
    }

    public ReviewerProfile assignSubmission(String reviewerUserId, String submissionId) {
        ReviewerProfile profile = reviewerProfileRepository.findByUserId(reviewerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer profile not found for userId: " + reviewerUserId));

        profile.addAssignedSubmissionId(submissionId);
        return reviewerProfileRepository.save(profile);
    }

    public ReviewerProfile markReviewCompleted(String reviewerUserId) {
        ReviewerProfile profile = reviewerProfileRepository.findByUserId(reviewerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer profile not found for userId: " + reviewerUserId));

        profile.markReviewCompleted();
        return reviewerProfileRepository.save(profile);
    }

    public void deleteProfile(String id) {
        reviewerProfileRepository.deleteById(id);
    }
}
