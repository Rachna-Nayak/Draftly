package com.draftly.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.draftly.model.Session;
import com.draftly.repository.SessionRepository;

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

    public Optional<Session> getValidSessionByToken(String token) {
        return sessionRepository.findByToken(token)
                .filter(Session::isActive)
                .filter(session -> session.getExpiresAt() != null && session.getExpiresAt().isAfter(LocalDateTime.now()));
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

    public void deactivateSessionIfPresent(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            if (session.isActive()) {
                session.setActive(false);
                sessionRepository.save(session);
            }
        });
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
