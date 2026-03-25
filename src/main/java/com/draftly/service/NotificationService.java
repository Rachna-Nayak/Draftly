package com.draftly.service;

import com.draftly.model.NotificationLog;
import com.draftly.repository.NotificationLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles platform notification events (FR11).
 *
 * Logs are persisted in MongoDB through NotificationLogRepository.
 */
@Service
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationService(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    /**
     * Generic notification sender.
     */
    public NotificationLog sendNotification(String actorUserId,
                                            String recipientUserId,
                                            String channel,
                                            String actionType,
                                            String message) {
        NotificationLog log = new NotificationLog(
                actorUserId,
                actionType,
                message,
                recipientUserId,
                channel,
                "SENT"
        );
        return notificationLogRepository.save(log);
    }

    /**
     * Event: Paper submission confirmation to author.
     */
    public NotificationLog notifyPaperSubmitted(String authorUserId, String submissionId) {
        return sendNotification(
                authorUserId,
                authorUserId,
                "SYSTEM_ALERT",
                "PAPER_SUBMITTED",
                "Submission " + submissionId + " has been received successfully."
        );
    }

    /**
     * Event: Reviewer assignment notification.
     */
    public NotificationLog notifyReviewerAssigned(String adminUserId, String reviewerUserId, String submissionId) {
        return sendNotification(
                adminUserId,
                reviewerUserId,
                "SYSTEM_ALERT",
                "REVIEWER_ASSIGNED",
                "You have been assigned to review submission " + submissionId + "."
        );
    }

    /**
     * Event: Feedback submitted to author.
     */
    public NotificationLog notifyFeedbackSubmitted(String reviewerUserId, String authorUserId, String submissionId) {
        return sendNotification(
                reviewerUserId,
                authorUserId,
                "SYSTEM_ALERT",
                "FEEDBACK_SUBMITTED",
                "Feedback has been submitted for your submission " + submissionId + "."
        );
    }

    /**
     * Event: Review completed notification.
     */
    public NotificationLog notifyReviewCompleted(String reviewerUserId, String adminUserId, String submissionId) {
        return sendNotification(
                reviewerUserId,
                adminUserId,
                "SYSTEM_ALERT",
                "REVIEW_COMPLETED",
                "Review has been completed for submission " + submissionId + "."
        );
    }

    public List<NotificationLog> getAllLogs() {
        return notificationLogRepository.findAll();
    }
}
