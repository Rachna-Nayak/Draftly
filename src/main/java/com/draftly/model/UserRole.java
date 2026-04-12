package com.draftly.model;

/**
 * Enum representing the roles a user can have in the platform.
 */
public enum UserRole {
    AUTHOR,
    REVIEWER,
    ADMIN,

    // Legacy roles kept for backward compatibility with existing records.
    STUDENT_RESEARCHER,
    FACULTY_SUPERVISOR,
    SYSTEM_ADMINISTRATOR;

    public UserRole normalized() {
        return switch (this) {
            case AUTHOR, STUDENT_RESEARCHER -> AUTHOR;
            case REVIEWER, FACULTY_SUPERVISOR -> REVIEWER;
            case ADMIN, SYSTEM_ADMINISTRATOR -> ADMIN;
        };
    }

    public boolean matches(UserRole expected) {
        if (expected == null) {
            return false;
        }
        return this.normalized() == expected.normalized();
    }

    public boolean matchesAny(UserRole... roles) {
        if (roles == null || roles.length == 0) {
            return true;
        }

        for (UserRole role : roles) {
            if (matches(role)) {
                return true;
            }
        }
        return false;
    }

    public static UserRole fromExternalValue(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return AUTHOR;
        }

        String normalized = rawRole.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "AUTHOR", "STUDENT_RESEARCHER" -> AUTHOR;
            case "REVIEWER", "FACULTY_SUPERVISOR" -> REVIEWER;
            case "ADMIN", "SYSTEM_ADMINISTRATOR" -> ADMIN;
            default -> throw new IllegalArgumentException("Invalid role: " + rawRole);
        };
    }
}
