package com.draftly.security;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import com.draftly.model.Session;
import com.draftly.model.User;
import com.draftly.model.UserRole;
import com.draftly.service.SessionService;
import com.draftly.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Validates session token and applies RBAC checks for API endpoints.
 */
@Component
public class RoleBasedAccessInterceptor implements HandlerInterceptor {

    private static final String SESSION_TOKEN_HEADER = "X-Session-Token";

    private final SessionService sessionService;
    private final UserService userService;

    public RoleBasedAccessInterceptor(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();

        if (!requestUri.startsWith("/api/") || requestUri.startsWith("/api/auth/")) {
            return true;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(SESSION_TOKEN_HEADER);
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing session token");
        }

        Session session = sessionService.getValidSessionByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid or expired"));

        User user = userService.getUserById(session.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for session"));

        request.setAttribute("currentUserId", user.getId());
        request.setAttribute("currentUserRole", user.getRole().normalized());

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Optional<RequireRoles> methodRoles = Optional.ofNullable(handlerMethod.getMethodAnnotation(RequireRoles.class));
        Optional<RequireRoles> classRoles = Optional.ofNullable(handlerMethod.getBeanType().getAnnotation(RequireRoles.class));

        UserRole[] allowedRoles = methodRoles
                .or(() -> classRoles)
                .map(RequireRoles::value)
                .orElse(new UserRole[0]);

        if (allowedRoles.length > 0 && !user.getRole().matchesAny(allowedRoles)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this resource");
        }

        return true;
    }
}
