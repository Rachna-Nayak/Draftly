package com.draftly.controller;

import com.draftly.model.ChangeLog;
import com.draftly.service.SessionLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for session-related audit logs.
 */
@RestController
@RequestMapping("/api/session-logs")
public class SessionLogController {

    private final SessionLogService sessionLogService;

    public SessionLogController(SessionLogService sessionLogService) {
        this.sessionLogService = sessionLogService;
    }

    @PostMapping
    public ChangeLog logSessionEvent(@RequestBody Map<String, String> body) {
        return sessionLogService.logSessionEvent(
                body.get("userId"),
                body.get("actionType"),
                body.getOrDefault("message", "Session event"),
                body.get("sessionId"),
                body.getOrDefault("oldValue", ""),
                body.getOrDefault("newValue", "")
        );
    }

    @GetMapping
    public List<ChangeLog> getAllSessionLogs() {
        return sessionLogService.getAllSessionLogs();
    }

    @GetMapping("/user/{userId}")
    public List<ChangeLog> getByUser(@PathVariable String userId) {
        return sessionLogService.getSessionLogsByUser(userId);
    }

    @GetMapping("/action/{actionType}")
    public List<ChangeLog> getByAction(@PathVariable String actionType) {
        return sessionLogService.getSessionLogsByAction(actionType);
    }
}
