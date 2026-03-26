package com.draftly.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.draftly.model.PaperVersion;
import com.draftly.model.PublishedPaper;
import com.draftly.model.Reference;
import com.draftly.model.ResearchProject;
import com.draftly.model.Review;
import com.draftly.model.Submission;
import com.draftly.model.SubmissionFeedback;
import com.draftly.repository.PaperVersionRepository;
import com.draftly.repository.ProjectRepository;
import com.draftly.repository.PublishedPaperRepository;
import com.draftly.repository.ReferenceRepository;
import com.draftly.repository.ReviewRepository;
import com.draftly.repository.SubmissionFeedbackRepository;
import com.draftly.repository.SubmissionRepository;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final PaperVersionRepository paperVersionRepository;
    private final ReviewRepository reviewRepository;
    private final SubmissionFeedbackRepository submissionFeedbackRepository;
    private final PublishedPaperRepository publishedPaperRepository;
    private final ReferenceRepository referenceRepository;
    private final ProjectRepository projectRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                             PaperVersionRepository paperVersionRepository,
                             ReviewRepository reviewRepository,
                             SubmissionFeedbackRepository submissionFeedbackRepository,
                             PublishedPaperRepository publishedPaperRepository,
                             ReferenceRepository referenceRepository,
                             ProjectRepository projectRepository) {
        this.submissionRepository = submissionRepository;
        this.paperVersionRepository = paperVersionRepository;
        this.reviewRepository = reviewRepository;
        this.submissionFeedbackRepository = submissionFeedbackRepository;
        this.publishedPaperRepository = publishedPaperRepository;
        this.referenceRepository = referenceRepository;
        this.projectRepository = projectRepository;
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
        return submissionRepository.save(submission);
    }

    public Review addReview(String submissionId, String reviewerId, int rating, String comments, Review.Decision decision) {
        Submission submission = getSubmissionById(submissionId);
        if (submission.getStatus() != Submission.Status.SUBMITTED && submission.getStatus() != Submission.Status.UNDER_REVIEW) {
            throw new IllegalStateException("Submission must be in SUBMITTED or UNDER_REVIEW state for review");
        }

        submission.setStatus(Submission.Status.UNDER_REVIEW);
        submission.setUpdatedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        Review review = reviewRepository.save(new Review(submissionId, reviewerId, rating, comments, decision));
        submissionFeedbackRepository.save(new SubmissionFeedback(submissionId, reviewerId, comments));

        switch (decision) {
            case ACCEPT -> submission.setStatus(Submission.Status.ACCEPTED);
            case REVISION -> {
                submission.setStatus(Submission.Status.REVISION_REQUIRED);
                submission.setLocked(false);
            }
            case REJECT -> submission.setStatus(Submission.Status.REJECTED);
        }
        submission.setUpdatedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        return review;
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
}
