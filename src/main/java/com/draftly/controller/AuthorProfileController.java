package com.draftly.controller;

import com.draftly.model.AuthorProfile;
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
    public AuthorProfile createProfile(@RequestBody AuthorProfile authorProfile) {
        return authorProfileService.createProfile(authorProfile);
    }

    @GetMapping
    public List<AuthorProfile> listProfiles() {
        return authorProfileService.getAllProfiles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorProfile> getById(@PathVariable String id) {
        return authorProfileService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<AuthorProfile> getByUserId(@PathVariable String userId) {
        return authorProfileService.getByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/domain/{domain}")
    public List<AuthorProfile> byDomain(@PathVariable String domain) {
        return authorProfileService.findByDomain(domain);
    }

    @PostMapping("/add-project")
    public AuthorProfile addProject(@RequestBody Map<String, String> body) {
        return authorProfileService.addProject(
                body.get("userId"),
                body.get("projectId")
        );
    }

    @PostMapping("/add-submission")
    public AuthorProfile addSubmission(@RequestBody Map<String, String> body) {
        return authorProfileService.addSubmission(
                body.get("userId"),
                body.get("submissionId")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        authorProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
