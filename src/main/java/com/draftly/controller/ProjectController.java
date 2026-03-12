package com.draftly.controller;

import com.draftly.model.ResearchProject;
import com.draftly.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Research Project management. (UC1)
 */
@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public String listProjects(@RequestParam(required = false) String ownerId, Model model) {
        List<ResearchProject> projects;
        if (ownerId != null) {
            projects = projectService.getProjectsByOwner(ownerId);
        } else {
            projects = projectService.getProjectsByOwner("default"); // TODO: replace with session user
        }
        model.addAttribute("projects", projects);
        return "project/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new ResearchProject());
        return "project/create";
    }

    @PostMapping("/new")
    public String createProject(@RequestParam String title,
                                @RequestParam String domain,
                                @RequestParam String objectives,
                                @RequestParam(defaultValue = "default") String ownerId) {
        projectService.createProject(title, domain, objectives, ownerId);
        return "redirect:/projects?ownerId=" + ownerId;
    }

    @GetMapping("/{id}")
    public String viewProject(@PathVariable String id, Model model) {
        projectService.getProjectById(id).ifPresent(p -> model.addAttribute("project", p));
        return "project/view";
    }

    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable String id) {
        projectService.deleteProject(id);
        return "redirect:/projects";
    }
}
