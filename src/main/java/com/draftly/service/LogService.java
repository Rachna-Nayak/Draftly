package com.draftly.service;

import com.draftly.model.ChangeLog;
import com.draftly.repository.ChangeLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Central service for audit logging and retrieval.
 */
@Service
public class LogService {

    private final ChangeLogRepository changeLogRepository;

    public LogService(ChangeLogRepository changeLogRepository) {
        this.changeLogRepository = changeLogRepository;
    }

    public ChangeLog logChange(String userId,
                               String actionType,
                               String message,
                               String entityType,
                               String entityId,
                               String changeSummary,
                               String oldValue,
                               String newValue) {
        ChangeLog changeLog = new ChangeLog(
                userId,
                actionType,
                message,
                entityType,
                entityId,
                changeSummary,
                oldValue,
                newValue
        );
        return changeLogRepository.save(changeLog);
    }

    public List<ChangeLog> getAllLogs() {
        return changeLogRepository.findAll();
    }

    public List<ChangeLog> getLogsByEntityType(String entityType) {
        return changeLogRepository.findByEntityType(entityType);
    }

    public List<ChangeLog> getLogsByEntityId(String entityId) {
        return changeLogRepository.findByEntityId(entityId);
    }

    public List<ChangeLog> getLogsByActionType(String actionType) {
        return changeLogRepository.findByActionType(actionType);
    }

    public List<ChangeLog> getLogsByUserId(String userId) {
        return changeLogRepository.findByUserId(userId);
    }
}
