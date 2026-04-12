package com.draftly.controller;

import com.draftly.model.ResearchProject;
import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Research Project management. (UC1)
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<ResearchProject> listProjects(@RequestParam(required = false) String ownerId,
                                              HttpServletRequest request) {
        UserRole currentRole = (UserRole) request.getAttribute("currentUserRole");
        String currentUserId = (String) request.getAttribute("currentUserId");

        if (currentRole != null && currentRole.matches(UserRole.ADMIN) && (ownerId == null || ownerId.isBlank())) {
            return projectService.getAllProjects();
        }

        String effectiveOwnerId = (ownerId == null || ownerId.isBlank()) ? currentUserId : ownerId;
        if (effectiveOwnerId == null || effectiveOwnerId.isBlank()) {
            effectiveOwnerId = "default";
        }
        return projectService.getProjectsByOwner(effectiveOwnerId);
    }

    @PostMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public ResearchProject createProject(@RequestBody Map<String, String> body) {
        return projectService.createProject(
            body.get("title"),
            body.get("domain"),
            body.get("objectives"),
            body.getOrDefault("ownerId", "default")
        );
    }

    @GetMapping("/{id}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<ResearchProject> getProject(@PathVariable String id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
