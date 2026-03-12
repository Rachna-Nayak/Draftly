package com.draftly.controller;

import com.draftly.model.ResearchProject;
import com.draftly.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public List<ResearchProject> listProjects(@RequestParam(defaultValue = "default") String ownerId) {
        return projectService.getProjectsByOwner(ownerId);
    }

    @PostMapping
    public ResearchProject createProject(@RequestBody Map<String, String> body) {
        return projectService.createProject(
            body.get("title"),
            body.get("domain"),
            body.get("objectives"),
            body.getOrDefault("ownerId", "default")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResearchProject> getProject(@PathVariable String id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
