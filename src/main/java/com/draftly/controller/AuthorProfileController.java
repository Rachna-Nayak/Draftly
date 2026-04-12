package com.draftly.controller;

import com.draftly.model.AuthorProfile;
import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.AuthorProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for author profile management.
 */
@RestController
@RequestMapping("/api/author-profiles")
public class AuthorProfileController {

    private final AuthorProfileService authorProfileService;

    public AuthorProfileController(AuthorProfileService authorProfileService) {
        this.authorProfileService = authorProfileService;
    }

    @PostMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public AuthorProfile createProfile(@RequestBody AuthorProfile authorProfile) {
        return authorProfileService.createProfile(authorProfile);
    }

    @GetMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public List<AuthorProfile> listProfiles() {
        return authorProfileService.getAllProfiles();
    }

    @GetMapping("/{id}")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public ResponseEntity<AuthorProfile> getById(@PathVariable String id) {
        return authorProfileService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public ResponseEntity<AuthorProfile> getByUserId(@PathVariable String userId) {
        return authorProfileService.getByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/domain/{domain}")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public List<AuthorProfile> byDomain(@PathVariable String domain) {
        return authorProfileService.findByDomain(domain);
    }

    @PostMapping("/add-project")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public AuthorProfile addProject(@RequestBody Map<String, String> body) {
        return authorProfileService.addProject(
                body.get("userId"),
                body.get("projectId")
        );
    }

    @PostMapping("/add-submission")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public AuthorProfile addSubmission(@RequestBody Map<String, String> body) {
        return authorProfileService.addSubmission(
                body.get("userId"),
                body.get("submissionId")
        );
    }

    @DeleteMapping("/{id}")
    @RequireRoles({UserRole.ADMIN})
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        authorProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
