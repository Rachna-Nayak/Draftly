package com.draftly.repository;

import com.draftly.model.ChangeLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying session-related audit logs.
 * Uses ChangeLog entries where entityType/actionType represent session lifecycle events.
 */
@Repository
public interface SessionLogRepository extends MongoRepository<ChangeLog, String> {
    List<ChangeLog> findByEntityType(String entityType);
    List<ChangeLog> findByUserId(String userId);
    List<ChangeLog> findByActionType(String actionType);
}
