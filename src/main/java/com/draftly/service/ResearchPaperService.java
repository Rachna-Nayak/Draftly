package com.draftly.service;

import com.draftly.model.PaperSection;
import com.draftly.model.ResearchPaper;
import com.draftly.repository.ResearchPaperRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Research Paper Service - Manages the student's written papers (sections, status).
 */
@Service
public class ResearchPaperService {

    private final ResearchPaperRepository researchPaperRepository;

    public ResearchPaperService(ResearchPaperRepository researchPaperRepository) {
        this.researchPaperRepository = researchPaperRepository;
    }

    public ResearchPaper createPaper(String projectId, String title) {
        ResearchPaper paper = new ResearchPaper(projectId, title);
        return researchPaperRepository.save(paper);
    }

    public Optional<ResearchPaper> getPaperById(String id) {
        return researchPaperRepository.findById(id);
    }

    public List<ResearchPaper> getPapersByProject(String projectId) {
        return researchPaperRepository.findByProjectId(projectId);
    }

    public ResearchPaper addSection(String paperId, String sectionName, String content, int order) {
        ResearchPaper paper = researchPaperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found: " + paperId));
        paper.addSection(new PaperSection(sectionName, content, order));
        return researchPaperRepository.save(paper);
    }

    public ResearchPaper updateSection(String paperId, String sectionName, String newContent) {
        ResearchPaper paper = researchPaperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found: " + paperId));
        paper.getSections().stream()
                .filter(s -> s.getSectionName().equals(sectionName))
                .findFirst()
                .ifPresent(s -> s.setContent(newContent));
        return researchPaperRepository.save(paper);
    }

    public void deletePaper(String paperId) {
        researchPaperRepository.deleteById(paperId);
    }
}
