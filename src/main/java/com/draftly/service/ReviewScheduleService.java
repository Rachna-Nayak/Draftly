package com.draftly.service;

import com.draftly.model.ReviewSchedule;
import com.draftly.model.ReviewerProfile;
import com.draftly.model.Submission;
import com.draftly.model.Conference;
import com.draftly.repository.ConferenceRepository;
import com.draftly.repository.ReviewScheduleRepository;
import com.draftly.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ReviewScheduleService {

    private final ReviewScheduleRepository reviewScheduleRepository;
    private final ReviewerProfileService reviewerProfileService;
    private final NotificationService notificationService;
    private final SubmissionRepository submissionRepository;
    private final ConferenceRepository conferenceRepository;

    public ReviewScheduleService(ReviewScheduleRepository reviewScheduleRepository,
                                 ReviewerProfileService reviewerProfileService,
                                 NotificationService notificationService,
                                 SubmissionRepository submissionRepository,
                                 ConferenceRepository conferenceRepository) {
        this.reviewScheduleRepository = reviewScheduleRepository;
        this.reviewerProfileService = reviewerProfileService;
        this.notificationService = notificationService;
        this.submissionRepository = submissionRepository;
        this.conferenceRepository = conferenceRepository;
    }

    public ReviewSchedule createSchedule(ReviewSchedule schedule, String adminUserId) {
        enforceReviewerExpertiseMatch(schedule);
        ReviewSchedule saved = reviewScheduleRepository.save(schedule);

        if (saved.getReviewerUserId() != null && !saved.getReviewerUserId().isBlank()
                && saved.getSubmissionId() != null && !saved.getSubmissionId().isBlank()) {
            reviewerProfileService.assignSubmission(saved.getReviewerUserId(), saved.getSubmissionId());
            notificationService.notifyReviewerAssigned(
                    adminUserId == null || adminUserId.isBlank() ? saved.getReviewerUserId() : adminUserId,
                    saved.getReviewerUserId(),
                    saved.getSubmissionId()
            );
        }

        return saved;
    }

    private void enforceReviewerExpertiseMatch(ReviewSchedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("Review schedule payload is required");
        }

        if (schedule.getSubmissionId() == null || schedule.getSubmissionId().isBlank()) {
            throw new IllegalArgumentException("submissionId is required for reviewer assignment");
        }

        if (schedule.getReviewerUserId() == null || schedule.getReviewerUserId().isBlank()) {
            throw new IllegalArgumentException("reviewerUserId is required for reviewer assignment");
        }

        Submission submission = submissionRepository.findById(schedule.getSubmissionId())
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + schedule.getSubmissionId()));

        ReviewerProfile reviewerProfile = reviewerProfileService.getByUserId(schedule.getReviewerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Reviewer profile not found for userId: " + schedule.getReviewerUserId()));

        String submissionTrack = normalizeValue(submission.getTrack());
        String conferenceDomain = resolveConferenceDomain(schedule.getConferenceId());

        if (!matchesExpertise(reviewerProfile, submissionTrack, conferenceDomain)) {
            throw new IllegalArgumentException(
                    "Reviewer expertise does not match submission track/domain. " +
                    "Track='" + submission.getTrack() + "', Domain='" + conferenceDomain + "'"
            );
        }
    }

    private String resolveConferenceDomain(String conferenceId) {
        if (conferenceId == null || conferenceId.isBlank()) {
            return "";
        }

        Optional<Conference> conference = conferenceRepository.findById(conferenceId);
        return conference.map(value -> normalizeValue(value.getDomain())).orElse("");
    }

    private boolean matchesExpertise(ReviewerProfile reviewerProfile, String submissionTrack, String conferenceDomain) {
        if (reviewerProfile.getExpertiseDomains() == null || reviewerProfile.getExpertiseDomains().isEmpty()) {
            return false;
        }

        List<String> expertiseDomains = reviewerProfile.getExpertiseDomains().stream()
                .filter(domain -> domain != null && !domain.isBlank())
                .map(this::normalizeValue)
                .toList();

        if (expertiseDomains.isEmpty()) {
            return false;
        }

        for (String expertise : expertiseDomains) {
            if (!submissionTrack.isBlank() && (expertise.contains(submissionTrack) || submissionTrack.contains(expertise))) {
                return true;
            }
            if (!conferenceDomain.isBlank() && (expertise.contains(conferenceDomain) || conferenceDomain.contains(expertise))) {
                return true;
            }
        }

        return false;
    }

    private String normalizeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public ReviewSchedule createSchedule(String conferenceId,
                                         String submissionId,
                                         String reviewerUserId,
                                         LocalDateTime scheduledAt,
                                         LocalDateTime dueAt,
                                         String adminUserId) {
        return createSchedule(new ReviewSchedule(conferenceId, submissionId, reviewerUserId, scheduledAt, dueAt, adminUserId), adminUserId);
    }

    public Optional<ReviewSchedule> getById(String id) {
        return reviewScheduleRepository.findById(id);
    }

    public List<ReviewSchedule> getByConferenceId(String conferenceId) {
        return reviewScheduleRepository.findByConferenceId(conferenceId);
    }

    public List<ReviewSchedule> getBySubmissionId(String submissionId) {
        return reviewScheduleRepository.findBySubmissionId(submissionId);
    }

    public List<ReviewSchedule> getByReviewerUserId(String reviewerUserId) {
        return reviewScheduleRepository.findByReviewerUserId(reviewerUserId);
    }

    public List<ReviewSchedule> getAllSchedules() {
        return reviewScheduleRepository.findAll();
    }

    public ReviewSchedule markCompleted(String id) {
        ReviewSchedule schedule = reviewScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review schedule not found: " + id));
        schedule.setStatus(ReviewSchedule.Status.COMPLETED);
        return reviewScheduleRepository.save(schedule);
    }

    public void deleteSchedule(String id) {
        reviewScheduleRepository.deleteById(id);
    }
}