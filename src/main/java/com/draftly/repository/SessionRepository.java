package com.draftly.repository;

import com.draftly.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {
    Optional<Session> findByToken(String token);
    List<Session> findByUserId(String userId);
    List<Session> findByActiveTrue();
    List<Session> findByExpiresAtBefore(LocalDateTime cutoff);
}
