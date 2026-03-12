package com.draftly.service;

import com.draftly.model.ResearchProject;
import com.draftly.model.User;
import com.draftly.repository.ProjectRepository;
import com.draftly.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Project Service - Handles research project creation and management. (UC1)
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NLPService nlpService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, NLPService nlpService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.nlpService = nlpService;
    }

    /**
     * UC1: Create a new research project with auto-suggested keywords.
     */
    public ResearchProject createProject(String title, String domain, String objectives, String ownerId) {
        // Extract keywords from title + objectives using NLP
        List<String> suggestedKeywords = nlpService.extractKeywords(title + " " + objectives);

        ResearchProject project = new ResearchProject(title, domain, suggestedKeywords, objectives, ownerId);
        ResearchProject saved = projectRepository.save(project);

        // Add project to user's project list
        Optional<User> userOpt = userRepository.findById(ownerId);
        userOpt.ifPresent(user -> {
            user.addProjectId(saved.getId());
            userRepository.save(user);
        });

        return saved;
    }

    /**
     * Get all projects belonging to a user.
     */
    public List<ResearchProject> getProjectsByOwner(String ownerId) {
        return projectRepository.findByOwnerId(ownerId);
    }

    /**
     * Get a project by ID.
     */
    public Optional<ResearchProject> getProjectById(String projectId) {
        return projectRepository.findById(projectId);
    }

    /**
     * Update a project's details.
     */
    public ResearchProject updateProject(ResearchProject project) {
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    /**
     * Delete a project by ID.
     */
    public void deleteProject(String projectId) {
        projectRepository.deleteById(projectId);
    }
}
