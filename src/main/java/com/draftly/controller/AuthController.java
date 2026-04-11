package com.draftly.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.draftly.model.Session;
import com.draftly.model.User;
import com.draftly.model.UserRole;
import com.draftly.service.SessionService;
import com.draftly.service.UserService;

/**
 * Authentication endpoints used by the frontend login/register flow.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final SessionService sessionService;

    public AuthController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String name = required(body, "name");
        String email = required(body, "email");
        String password = required(body, "password");

        UserRole role = parseRole(body.getOrDefault("role", UserRole.STUDENT_RESEARCHER.name()));

        User user;
        try {
            user = userService.registerUser(name, email, password, role);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "An account with this email already exists. Please sign in instead."));
        }

        Session session = sessionService.createSession(user.getId(), 120);
        return ResponseEntity.status(HttpStatus.CREATED).body(authPayload(user, session.getToken()));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String email = required(body, "email");
        String password = required(body, "password");

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!userService.verifyPassword(user, password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        Session session = sessionService.createSession(user.getId(), 120);
        return authPayload(user, session.getToken());
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader(value = "X-Session-Token", required = false) String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing session token");
        }

        Session session = sessionService.getValidSessionByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid or expired"));

        User user = userService.getUserById(session.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for session"));

        return authPayload(user, session.getToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "X-Session-Token", required = false) String token) {
        if (token != null && !token.isBlank()) {
            sessionService.deactivateSessionIfPresent(token);
        }
        return ResponseEntity.noContent().build();
    }

    private static String required(Map<String, String> body, String field) {
        String value = body.get(field);
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    private static UserRole parseRole(String roleRaw) {
        try {
            return UserRole.valueOf(roleRaw);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleRaw);
        }
    }

    private static Map<String, Object> authPayload(User user, String sessionToken) {
        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "sessionToken", sessionToken
        );
    }
}
