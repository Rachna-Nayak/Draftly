package com.draftly.service;

import com.draftly.model.PaperSection;
import com.draftly.model.PlagiarismReport;
import com.draftly.model.Reference;
import com.draftly.model.ResearchPaper;
import com.draftly.repository.ResearchPaperRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Export Service - Validates formatting and exports papers. (UC8)
 */
@Service
public class ExportService {

    private final ResearchPaperRepository researchPaperRepository;
    private final ReferenceService referenceService;
    private final PlagiarismService plagiarismService;
    private final NLPService nlpService;

    public ExportService(ResearchPaperRepository researchPaperRepository,
                         ReferenceService referenceService,
                         PlagiarismService plagiarismService,
                         NLPService nlpService) {
        this.researchPaperRepository = researchPaperRepository;
        this.referenceService = referenceService;
        this.plagiarismService = plagiarismService;
        this.nlpService = nlpService;
    }

    /**
     * UC8: Run a submission readiness check on the paper.
     */
    public ReadinessReport checkReadiness(String researchPaperId, String projectId) {
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(researchPaperId);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("Research paper not found: " + researchPaperId);
        }

        ResearchPaper paper = paperOpt.get();
        ReadinessReport report = new ReadinessReport();

        // Check sections exist
        if (paper.getSections().isEmpty()) {
            report.addIssue("Paper has no sections.");
        }
        for (PaperSection section : paper.getSections()) {
            if (section.getContent() == null || section.getContent().isBlank()) {
                report.addIssue("Section '" + section.getSectionName() + "' is empty.");
            }
        }

        // Check references
        List<Reference> refs = referenceService.getReferencesByProject(projectId);
        if (refs.isEmpty()) {
            report.addIssue("No references found for project.");
        }

        // Run plagiarism check summary
        PlagiarismReport plagReport = plagiarismService.checkPlagiarism(researchPaperId);
        if (!plagReport.getFlaggedSections().isEmpty()) {
            report.addIssue("Plagiarism flags detected in " + plagReport.getFlaggedSections().size() + " section(s).");
        }

        report.setReady(report.getIssues().isEmpty());
        return report;
    }

    /**
     * UC8: Export paper as PDF.
     */
    public byte[] exportAsPDF(String researchPaperId) {
        // TODO: Implement PDF export using iText
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(researchPaperId);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("Research paper not found: " + researchPaperId);
        }
        // Placeholder
        return new byte[0];
    }

    /**
     * UC8: Export paper as Word document.
     */
    public byte[] exportAsWord(String researchPaperId) {
        // TODO: Implement Word export using Apache POI
        return new byte[0];
    }

    /**
     * UC8: Export paper as LaTeX.
     */
    public String exportAsLatex(String researchPaperId) {
        // TODO: Implement LaTeX template export
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(researchPaperId);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("Research paper not found: " + researchPaperId);
        }

        ResearchPaper paper = paperOpt.get();
        StringBuilder latex = new StringBuilder();
        latex.append("\\documentclass{article}\n");
        latex.append("\\title{").append(paper.getTitle()).append("}\n");
        latex.append("\\begin{document}\n");
        latex.append("\\maketitle\n\n");

        for (PaperSection section : paper.getSections()) {
            latex.append("\\section{").append(section.getSectionName()).append("}\n");
            latex.append(section.getContent()).append("\n\n");
        }

        latex.append("\\end{document}\n");
        return latex.toString();
    }

    /**
     * Inner class representing the readiness report for export.
     */
    public static class ReadinessReport {
        private boolean ready;
        private List<String> issues = new ArrayList<>();

        public boolean isReady() { return ready; }
        public void setReady(boolean ready) { this.ready = ready; }

        public List<String> getIssues() { return issues; }
        public void setIssues(List<String> issues) { this.issues = issues; }

        public void addIssue(String issue) { this.issues.add(issue); }
    }
}
