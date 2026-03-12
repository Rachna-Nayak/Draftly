package com.draftly.service;

import com.draftly.model.Paper;
import com.draftly.model.Reference;
import com.draftly.model.ResearchProject;
import com.draftly.repository.PaperRepository;
import com.draftly.repository.ProjectRepository;
import com.draftly.repository.ReferenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Reference Service - Manages references and citation generation. (UC4)
 */
@Service
public class ReferenceService {

    private final ReferenceRepository referenceRepository;
    private final PaperRepository paperRepository;
    private final ProjectRepository projectRepository;
    private final CredibilityService credibilityService;

    public ReferenceService(ReferenceRepository referenceRepository, PaperRepository paperRepository,
                            ProjectRepository projectRepository, CredibilityService credibilityService) {
        this.referenceRepository = referenceRepository;
        this.paperRepository = paperRepository;
        this.projectRepository = projectRepository;
        this.credibilityService = credibilityService;
    }

    /**
     * UC4: Add a paper as a reference to a project with generated citation.
     */
    public Reference addReference(String projectId, String paperId, String format) {
        Optional<Paper> paperOpt = paperRepository.findById(paperId);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("Paper not found: " + paperId);
        }

        Paper paper = paperOpt.get();
        String citation = generateCitation(paper, format);

        Reference reference = new Reference(projectId, paperId, format, citation);
        Reference saved = referenceRepository.save(reference);

        // Add reference to project
        Optional<ResearchProject> projectOpt = projectRepository.findById(projectId);
        projectOpt.ifPresent(project -> {
            project.addReferenceId(saved.getId());
            projectRepository.save(project);
        });

        return saved;
    }

    /**
     * UC4: Get all references for a project.
     */
    public List<Reference> getReferencesByProject(String projectId) {
        return referenceRepository.findByProjectId(projectId);
    }

    /**
     * UC4: Generate citation in the specified format.
     */
    public String generateCitation(Paper paper, String format) {
        return switch (format.toUpperCase()) {
            case "MLA" -> credibilityService.generateCitationMLA(paper);
            case "IEEE" -> credibilityService.generateCitationIEEE(paper);
            default -> credibilityService.generateCitationAPA(paper);
        };
    }

    /**
     * UC4: Suggest additional references based on project keywords.
     */
    public List<Paper> suggestReferences(String projectId) {
        // TODO: Implement abstract similarity comparison using NLP
        Optional<ResearchProject> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return List.of();
        }
        ResearchProject project = projectOpt.get();
        return paperRepository.findByKeywordsIn(project.getKeywords());
    }

    /**
     * Remove a reference.
     */
    public void removeReference(String referenceId) {
        referenceRepository.deleteById(referenceId);
    }
}
