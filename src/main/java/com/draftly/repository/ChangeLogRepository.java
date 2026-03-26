package com.draftly.repository;

import com.draftly.model.ChangeLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeLogRepository extends MongoRepository<ChangeLog, String> {
    List<ChangeLog> findByEntityType(String entityType);
    List<ChangeLog> findByEntityId(String entityId);
    List<ChangeLog> findByActionType(String actionType);
    List<ChangeLog> findByUserId(String userId);
}
