package com.draftly.service;

import com.draftly.model.ChangeLog;
import com.draftly.repository.SessionLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for session-related audit logging and queries.
 */
@Service
public class SessionLogService {

    private static final String SESSION_ENTITY_TYPE = "SESSION";

    private final SessionLogRepository sessionLogRepository;

    public SessionLogService(SessionLogRepository sessionLogRepository) {
        this.sessionLogRepository = sessionLogRepository;
    }

    public ChangeLog logSessionEvent(String userId,
                                     String actionType,
                                     String message,
                                     String sessionId,
                                     String oldValue,
                                     String newValue) {
        ChangeLog log = new ChangeLog(
                userId,
                actionType,
                message,
                SESSION_ENTITY_TYPE,
                sessionId,
                "Session lifecycle event",
                oldValue,
                newValue
        );
        return sessionLogRepository.save(log);
    }

    public List<ChangeLog> getAllSessionLogs() {
        return sessionLogRepository.findByEntityType(SESSION_ENTITY_TYPE);
    }

    public List<ChangeLog> getSessionLogsByUser(String userId) {
        return sessionLogRepository.findByUserId(userId);
    }

    public List<ChangeLog> getSessionLogsByAction(String actionType) {
        return sessionLogRepository.findByActionType(actionType);
    }
}
