package com.draftly.controller;

import com.draftly.model.ReviewSchedule;
import com.draftly.service.ReviewScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review-schedules")
public class ReviewScheduleController {

    private final ReviewScheduleService reviewScheduleService;

    public ReviewScheduleController(ReviewScheduleService reviewScheduleService) {
        this.reviewScheduleService = reviewScheduleService;
    }

    @PostMapping
    public ReviewSchedule createSchedule(@RequestBody Map<String, String> body) {
        return reviewScheduleService.createSchedule(
                body.get("conferenceId"),
                body.get("submissionId"),
                body.get("reviewerUserId"),
                body.containsKey("scheduledAt") && body.get("scheduledAt") != null && !body.get("scheduledAt").isBlank()
                        ? LocalDateTime.parse(body.get("scheduledAt"))
                        : LocalDateTime.now(),
                body.containsKey("dueAt") && body.get("dueAt") != null && !body.get("dueAt").isBlank()
                        ? LocalDateTime.parse(body.get("dueAt"))
                        : LocalDateTime.now().plusDays(7),
                body.get("adminUserId")
        );
    }

    @GetMapping
    public List<ReviewSchedule> listSchedules() {
        return reviewScheduleService.getAllSchedules();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewSchedule> getById(@PathVariable String id) {
        return reviewScheduleService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/conference/{conferenceId}")
    public List<ReviewSchedule> getByConferenceId(@PathVariable String conferenceId) {
        return reviewScheduleService.getByConferenceId(conferenceId);
    }

    @GetMapping("/submission/{submissionId}")
    public List<ReviewSchedule> getBySubmissionId(@PathVariable String submissionId) {
        return reviewScheduleService.getBySubmissionId(submissionId);
    }

    @GetMapping("/reviewer/{reviewerUserId}")
    public List<ReviewSchedule> getByReviewerUserId(@PathVariable String reviewerUserId) {
        return reviewScheduleService.getByReviewerUserId(reviewerUserId);
    }

    @PostMapping("/{id}/complete")
    public ReviewSchedule markCompleted(@PathVariable String id) {
        return reviewScheduleService.markCompleted(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable String id) {
        reviewScheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}