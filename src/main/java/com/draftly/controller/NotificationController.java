package com.draftly.controller;

import com.draftly.model.NotificationLog;
import com.draftly.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for notification workflows (FR11).
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationLog> listAll() {
        return notificationService.getAllLogs();
    }

    @PostMapping("/send")
    public NotificationLog send(@RequestBody Map<String, String> body) {
        return notificationService.sendNotification(
                body.get("actorUserId"),
                body.get("recipientUserId"),
                body.getOrDefault("channel", "SYSTEM_ALERT"),
                body.get("actionType"),
                body.get("message")
        );
    }

    @PostMapping("/events/paper-submitted")
    public NotificationLog notifyPaperSubmitted(@RequestBody Map<String, String> body) {
        return notificationService.notifyPaperSubmitted(
                body.get("authorUserId"),
                body.get("submissionId")
        );
    }

    @PostMapping("/events/reviewer-assigned")
    public NotificationLog notifyReviewerAssigned(@RequestBody Map<String, String> body) {
        return notificationService.notifyReviewerAssigned(
                body.get("adminUserId"),
                body.get("reviewerUserId"),
                body.get("submissionId")
        );
    }

    @PostMapping("/events/feedback-submitted")
    public NotificationLog notifyFeedbackSubmitted(@RequestBody Map<String, String> body) {
        return notificationService.notifyFeedbackSubmitted(
                body.get("reviewerUserId"),
                body.get("authorUserId"),
                body.get("submissionId")
        );
    }

    @PostMapping("/events/review-completed")
    public NotificationLog notifyReviewCompleted(@RequestBody Map<String, String> body) {
        return notificationService.notifyReviewCompleted(
                body.get("reviewerUserId"),
                body.get("adminUserId"),
                body.get("submissionId")
        );
    }
}
