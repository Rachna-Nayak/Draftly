package com.draftly.controller;

import com.draftly.model.Session;
import com.draftly.service.SessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for session lifecycle operations (FR1).
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public Session createSession(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        long durationMinutes = Long.parseLong(body.getOrDefault("durationMinutes", "120"));
        return sessionService.createSession(userId, durationMinutes);
    }

    @GetMapping("/token/{token}")
    public Session getByToken(@PathVariable String token) {
        return sessionService.getByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Session not found for token: " + token));
    }

    @GetMapping("/user/{userId}")
    public List<Session> getByUserId(@PathVariable String userId) {
        return sessionService.getByUserId(userId);
    }

    @GetMapping("/active")
    public List<Session> getActiveSessions() {
        return sessionService.getActiveSessions();
    }

    @PostMapping("/deactivate")
    public Session deactivateSession(@RequestBody Map<String, String> body) {
        return sessionService.deactivateSession(body.get("token"));
    }

    @PostMapping("/cleanup-expired")
    public Map<String, Integer> cleanupExpiredSessions() {
        int cleaned = sessionService.cleanupExpiredSessions();
        return Map.of("cleanedCount", cleaned);
    }
}
