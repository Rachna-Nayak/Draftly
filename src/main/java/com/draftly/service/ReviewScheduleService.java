package com.draftly.service;

import com.draftly.model.ReviewSchedule;
import com.draftly.repository.ReviewScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewScheduleService {

    private final ReviewScheduleRepository reviewScheduleRepository;
    private final ReviewerProfileService reviewerProfileService;
    private final NotificationService notificationService;

    public ReviewScheduleService(ReviewScheduleRepository reviewScheduleRepository,
                                 ReviewerProfileService reviewerProfileService,
                                 NotificationService notificationService) {
        this.reviewScheduleRepository = reviewScheduleRepository;
        this.reviewerProfileService = reviewerProfileService;
        this.notificationService = notificationService;
    }

    public ReviewSchedule createSchedule(ReviewSchedule schedule, String adminUserId) {
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