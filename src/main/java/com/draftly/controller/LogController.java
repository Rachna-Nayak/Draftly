package com.draftly.controller;

import com.draftly.model.ChangeLog;
import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.LogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for audit logs.
 */
@RestController
@RequestMapping("/api/logs")
@RequireRoles({UserRole.ADMIN})
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping
    public ChangeLog createLog(@RequestBody Map<String, String> body) {
        return logService.logChange(
                body.get("userId"),
                body.get("actionType"),
                body.getOrDefault("message", "Audit event"),
                body.get("entityType"),
                body.get("entityId"),
                body.getOrDefault("changeSummary", ""),
                body.getOrDefault("oldValue", ""),
                body.getOrDefault("newValue", "")
        );
    }

    @GetMapping
    public List<ChangeLog> listAll() {
        return logService.getAllLogs();
    }

    @GetMapping("/entity-type/{entityType}")
    public List<ChangeLog> byEntityType(@PathVariable String entityType) {
        return logService.getLogsByEntityType(entityType);
    }

    @GetMapping("/entity/{entityId}")
    public List<ChangeLog> byEntityId(@PathVariable String entityId) {
        return logService.getLogsByEntityId(entityId);
    }

    @GetMapping("/action/{actionType}")
    public List<ChangeLog> byActionType(@PathVariable String actionType) {
        return logService.getLogsByActionType(actionType);
    }

    @GetMapping("/user/{userId}")
    public List<ChangeLog> byUserId(@PathVariable String userId) {
        return logService.getLogsByUserId(userId);
    }
}
