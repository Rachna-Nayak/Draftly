package com.draftly.repository;

import com.draftly.model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
    List<NotificationLog> findByRecipientUserId(String recipientUserId);
    List<NotificationLog> findByStatus(String status);
    List<NotificationLog> findByChannel(String channel);
}
