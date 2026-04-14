package com.draftly.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.draftly.dto.SubmissionReviewContext;
import com.draftly.model.PaperVersion;
import com.draftly.model.PublishedPaper;
import com.draftly.model.Reference;
import com.draftly.model.ResearchProject;
import com.draftly.model.Review;
import com.draftly.model.ReviewSchedule;
import com.draftly.model.Submission;
import com.draftly.model.SubmissionFeedback;
import com.draftly.model.User;
import com.draftly.repository.PaperVersionRepository;
import com.draftly.repository.ProjectRepository;
import com.draftly.repository.PublishedPaperRepository;
import com.draftly.repository.ReferenceRepository;
import com.draftly.repository.ReviewRepository;
import com.draftly.repository.ReviewScheduleRepository;
import com.draftly.repository.SubmissionFeedbackRepository;
import com.draftly.repository.SubmissionRepository;
import com.draftly.repository.UserRepository;
import com.draftly.util.ReviewConsensusCalculator;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final PaperVersionRepository paperVersionRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final SubmissionFeedbackRepository submissionFeedbackRepository;
    private final PublishedPaperRepository publishedPaperRepository;
    private final ReferenceRepository referenceRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public SubmissionService(SubmissionRepository submissionRepository,
                             PaperVersionRepository paperVersionRepository,
                             ReviewRepository reviewRepository,
                             ReviewScheduleRepository reviewScheduleRepository,
                             SubmissionFeedbackRepository submissionFeedbackRepository,
                             PublishedPaperRepository publishedPaperRepository,
                             ReferenceRepository referenceRepository,
                             ProjectRepository projectRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.submissionRepository = submissionRepository;
        this.paperVersionRepository = paperVersionRepository;
        this.reviewRepository = reviewRepository;
        this.reviewScheduleRepository = reviewScheduleRepository;
        this.submissionFeedbackRepository = submissionFeedbackRepository;
        this.publishedPaperRepository = publishedPaperRepository;
        this.referenceRepository = referenceRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Submission createSubmission(String projectId, String authorId, String title,
                                       String conferenceName, String track, String docxPath) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Research project not found: " + projectId));

        Submission submission = new Submission(projectId, authorId, title, conferenceName, track, docxPath);
        return submissionRepository.save(submission);
    }

    public List<Submission> getSubmissionsByAuthor(String authorId) {
        return submissionRepository.findByAuthorId(authorId);
    }

    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    public List<Submission> getSubmissionsByProject(String projectId) {
        return submissionRepository.findByProjectId(projectId);
    }

    public Submission getSubmissionById(String submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));
    }

    public PaperVersion uploadPaperVersion(String submissionId, String filePath, String notes) {
        Submission submission = getSubmissionById(submissionId);
        if (submission.isLocked()) {
            throw new IllegalStateException("Submission is locked and cannot accept new versions");
        }

        List<PaperVersion> versions = paperVersionRepository.findBySubmissionIdOrderByVersionNumberAsc(submissionId);
        int nextVersion = versions.isEmpty() ? 1 : versions.get(versions.size() - 1).getVersionNumber() + 1;
        PaperVersion saved = paperVersionRepository.save(new PaperVersion(submissionId, nextVersion, filePath, notes));

        submission.setDocxPath(filePath);
        submission.setUpdatedAt(LocalDateTime.now());
        submissionRepository.save(submission);
        return saved;
    }

    public List<PaperVersion> getVersions(String submissionId) {
        getSubmissionById(submissionId);
        return paperVersionRepository.findBySubmissionIdOrderByVersionNumberAsc(submissionId);
    }

    public Submission attachReference(String submissionId, String referenceId) {
        Submission submission = getSubmissionById(submissionId);
        if (submission.isLocked()) {
            throw new IllegalStateException("Submission is locked and cannot be edited");
        }

        Reference reference = referenceRepository.findById(referenceId)
                .orElseThrow(() -> new IllegalArgumentException("Reference not found: " + referenceId));

        ResearchProject project = projectRepository.findById(submission.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Research project not found: " + submission.getProjectId()));

        if (!reference.getProjectId().equals(project.getId())) {
            throw new IllegalArgumentException("Reference does not belong to submission project");
        }

        if (!submission.getReferenceIds().contains(referenceId)) {
            submission.addReferenceId(referenceId);
        }
        submission.setUpdatedAt(LocalDateTime.now());
        return submissionRepository.save(submission);
    }

    public Submission submitPaper(String submissionId) {
        Submission submission = getSubmissionById(submissionId);

        List<PaperVersion> versions = paperVersionRepository.findBySubmissionIdOrderByVersionNumberAsc(submissionId);
        if (versions.isEmpty()) {
            throw new IllegalStateException("At least one paper version is required before submitting");
        }

        submission.setStatus(Submission.Status.SUBMITTED);
        submission.setLocked(true);
        submission.setUpdatedAt(LocalDateTime.now());
        Submission saved = submissionRepository.save(submission);

        if (saved.getAuthorId() != null && !saved.getAuthorId().isBlank()) {
            notificationService.notifyPaperSubmitted(saved.getAuthorId(), saved.getId());
        }

        return saved;
    }

    /**
     * Add a review and update submission status based on consensus logic.
     * If all reviewers have reviewed, calculate final status using consensus.
     */
    public Review addReview(String submissionId, String reviewerId, int rating, String comments, Review.Decision decision) {
        Submission submission = getSubmissionById(submissionId);
        if (submission.getStatus() != Submission.Status.SUBMITTED &&
            submission.getStatus() != Submission.Status.UNDER_REVIEW &&
            submission.getStatus() != Submission.Status.REVISION_REQUIRED) {
            throw new IllegalStateException("Submission must be in SUBMITTED, UNDER_REVIEW, or REVISION_REQUIRED state for review");
        }

        // Set to UNDER_REVIEW when first review arrives
        if (submission.getStatus() == Submission.Status.SUBMITTED) {
            submission.setStatus(Submission.Status.UNDER_REVIEW);
        }
        submission.setUpdatedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        Review review = new Review(submissionId, reviewerId, rating, comments, decision);
        Review savedReview = reviewRepository.save(review);
        submissionFeedbackRepository.save(new SubmissionFeedback(submissionId, reviewerId, comments));

        // Calculate consensus status if all reviewers have reviewed
        List<ReviewSchedule> schedules = reviewScheduleRepository.findBySubmissionId(submissionId);
        int totalReviewersAssigned = schedules.size();

        if (totalReviewersAssigned == 0) {
            totalReviewersAssigned = 1;
        }

        List<Review> allReviews = reviewRepository.findBySubmissionId(submissionId);
        Submission.Status consensusStatus = ReviewConsensusCalculator.calculateConsensusStatus(allReviews, totalReviewersAssigned);

        // Only update status if consensus is reached (all reviews in) or is UNDER_REVIEW
        if (allReviews.size() == totalReviewersAssigned) {
            submission.setStatus(consensusStatus);

            // Unlock if REVISION_REQUIRED
            if (consensusStatus == Submission.Status.REVISION_REQUIRED) {
                submission.setLocked(false);
            } else {
                submission.setLocked(true);
            }
        }

        submission.setUpdatedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        return savedReview;
    }

    public SubmissionFeedback storeFeedback(String submissionId, String reviewerId, String message) {
        getSubmissionById(submissionId);
        return submissionFeedbackRepository.save(new SubmissionFeedback(submissionId, reviewerId, message));
    }

    public List<Review> getReviews(String submissionId) {
        getSubmissionById(submissionId);
        return reviewRepository.findBySubmissionId(submissionId);
    }

    public List<SubmissionFeedback> getFeedback(String submissionId) {
        getSubmissionById(submissionId);
        return submissionFeedbackRepository.findBySubmissionId(submissionId);
    }

    public PublishedPaper publishSubmission(String submissionId, String doi, String journal, int publicationYear) {
        Submission submission = getSubmissionById(submissionId);
        if (submission.getStatus() != Submission.Status.ACCEPTED) {
            throw new IllegalStateException("Only accepted submissions can be published");
        }

        PublishedPaper existing = publishedPaperRepository.findBySubmissionId(submissionId).orElse(null);
        if (existing != null) {
            return existing;
        }

        PublishedPaper publishedPaper = publishedPaperRepository
                .save(new PublishedPaper(submissionId, doi, journal, publicationYear));

        submission.setStatus(Submission.Status.PUBLISHED);
        submission.setUpdatedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        return publishedPaper;
    }

    public PublishedPaper getPublishedPaper(String submissionId) {
        return publishedPaperRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Published paper not found for submission: " + submissionId));
    }

    /**
     * Get submission context for reviewers with author information stripped (blind review).
     * Returns only metadata needed for reviewing without revealing author identity.
     */
    public SubmissionReviewContext getSubmissionForReview(String submissionId, String reviewerId) {
        Submission submission = getSubmissionById(submissionId);

        // Verify reviewer is assigned to this submission
        if (!canReviewerReview(submissionId, reviewerId)) {
            throw new IllegalStateException("You are not authorized to review this submission");
        }

        // Get total reviewers assigned
        List<ReviewSchedule> schedules = reviewScheduleRepository.findBySubmissionId(submissionId);
        int totalReviewers = schedules.size();

        // Get completed reviews
        List<Review> reviews = reviewRepository.findBySubmissionId(submissionId);
        int completedReviews = reviews.size();

        // Get reference count
        int referenceCount = submission.getReferenceIds() != null ? submission.getReferenceIds().size() : 0;

        // Extract abstract from project if available
        String abstractText = "";
        Optional<ResearchProject> project = projectRepository.findById(submission.getProjectId());
        if (project.isPresent()) {
            abstractText = project.get().getObjectives() != null ? project.get().getObjectives() : "";
        }

        // Create context WITHOUT author information (blind review)
        return new SubmissionReviewContext(
                submission.getId(),
                submission.getTitle(),
                submission.getConferenceName(),
                submission.getTrack(),
                submission.getCreatedAt(),
                getReviewerDueDate(submissionId, reviewerId),
                totalReviewers,
                completedReviews,
                abstractText,
                project.isPresent() ? project.get().getKeywords() : List.of(),
                submission.getDocxPath(),
                referenceCount,
                submission.getStatus().toString()
        );
    }

    /**
     * Check if a reviewer can review this submission.
     * Returns true if:
     * - Reviewer is assigned to the submission
     * - Deadline has not passed (or no deadline)
     */
    public boolean canReviewerReview(String submissionId, String reviewerId) {
        List<ReviewSchedule> schedules = reviewScheduleRepository.findBySubmissionId(submissionId);

        for (ReviewSchedule schedule : schedules) {
            if (schedule.getReviewerUserId().equals(reviewerId)) {
                // Check deadline
                if (schedule.getDueAt() != null && schedule.getDueAt().isBefore(LocalDateTime.now())) {
                    return false; // Deadline passed
                }
                return true;
            }
        }

        return false; // Reviewer not assigned
    }

    /**
     * Get the due date for a specific reviewer's review
     */
    private LocalDateTime getReviewerDueDate(String submissionId, String reviewerId) {
        List<ReviewSchedule> schedules = reviewScheduleRepository.findBySubmissionId(submissionId);

        for (ReviewSchedule schedule : schedules) {
            if (schedule.getReviewerUserId().equals(reviewerId)) {
                return schedule.getDueAt();
            }
        }

        return null;
    }
}
