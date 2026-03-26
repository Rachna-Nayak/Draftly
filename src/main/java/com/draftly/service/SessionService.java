package com.draftly.service;

import com.draftly.model.Session;
import com.draftly.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for user session lifecycle management (FR1).
 */
@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session createSession(String userId, long durationMinutes) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(durationMinutes);
        Session session = new Session(userId, token, expiresAt);
        return sessionRepository.save(session);
    }

    public Optional<Session> getByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    public List<Session> getByUserId(String userId) {
        return sessionRepository.findByUserId(userId);
    }

    public List<Session> getActiveSessions() {
        return sessionRepository.findByActiveTrue();
    }

    public Session deactivateSession(String token) {
        Session session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Session not found for token: " + token));
        session.setActive(false);
        return sessionRepository.save(session);
    }

    public int cleanupExpiredSessions() {
        List<Session> expired = sessionRepository.findByExpiresAtBefore(LocalDateTime.now());
        if (expired.isEmpty()) {
            return 0;
        }

        for (Session session : expired) {
            session.setActive(false);
        }
        sessionRepository.saveAll(expired);
        return expired.size();
    }
}
