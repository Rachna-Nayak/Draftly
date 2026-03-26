package com.draftly.service;

import com.draftly.model.PaperSection;
import com.draftly.model.ResearchPaper;
import com.draftly.repository.ResearchPaperRepository;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Research Paper Service - Manages the student's written papers (sections, status).
 */
@Service
public class ResearchPaperService {

    private static final Set<String> COMMON_SECTION_HEADINGS = Set.of(
            "abstract", "introduction", "related work", "background", "methodology", "methods",
            "materials and methods", "experiments", "results", "discussion", "conclusion",
            "conclusions", "future work", "acknowledgment", "acknowledgements", "references", "keywords"
    );

    private final ResearchPaperRepository researchPaperRepository;
    private final ReferenceService referenceService;

    public ResearchPaperService(ResearchPaperRepository researchPaperRepository,
                                ReferenceService referenceService) {
        this.researchPaperRepository = researchPaperRepository;
        this.referenceService = referenceService;
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

    public ResearchPaper importFromDocx(String projectId, String title, byte[] docxBytes, String template) throws IOException {
        return importFromDocx(projectId, title, docxBytes, template, null);
    }

    public ResearchPaper importFromDocx(String projectId, String title, byte[] docxBytes,
                                        String template, String numberedBibtexReferences) throws IOException {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("projectId is required.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required.");
        }
        if (docxBytes == null || docxBytes.length == 0) {
            throw new IllegalArgumentException("DOCX document is empty.");
        }

        String normalizedTemplate = validateAndNormalizeTemplate(template);

        List<PaperSection> parsedSections;
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            parsedSections = parseSections(document);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Document readability validation failed. Ensure it is a structured DOCX file.", e);
        }

        if (parsedSections.isEmpty()) {
            throw new IllegalArgumentException("Document readability validation failed. No recognizable section headings were found.");
        }

        ResearchPaper paper = new ResearchPaper(projectId, title);
        paper.setTemplate(normalizedTemplate);
        paper.setSections(parsedSections);
        ResearchPaper savedPaper = researchPaperRepository.save(paper);

        if (numberedBibtexReferences != null && !numberedBibtexReferences.isBlank()) {
            referenceService.saveNumberedBibtexReferences(projectId, numberedBibtexReferences);
        }

        return savedPaper;
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

    private String validateAndNormalizeTemplate(String template) {
        if (template == null || template.isBlank()) {
            return "IEEE";
        }

        String normalized = template.trim().toUpperCase(Locale.ROOT);
        if (!"IEEE".equals(normalized) && !"LNCS".equals(normalized)) {
            throw new IllegalArgumentException("Unsupported template. Allowed values: IEEE, LNCS.");
        }

        return normalized;
    }

    private List<PaperSection> parseSections(XWPFDocument document) {
        List<PaperSection> sections = new ArrayList<>();
        String currentSectionName = null;
        StringBuilder currentContent = new StringBuilder();
        int order = 1;

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String rawText = paragraph.getText();
            if (rawText == null) {
                continue;
            }

            String text = rawText.trim();
            if (text.isEmpty()) {
                continue;
            }

            if (isSectionHeading(paragraph, text)) {
                if (currentSectionName != null) {
                    sections.add(new PaperSection(currentSectionName, currentContent.toString().trim(), order++));
                    currentContent.setLength(0);
                }
                currentSectionName = normalizeHeading(text);
                continue;
            }

            if (currentSectionName == null) {
                currentSectionName = "Abstract";
            }

            if (currentContent.length() > 0) {
                currentContent.append("\n");
            }
            currentContent.append(text);
        }

        if (currentSectionName != null) {
            sections.add(new PaperSection(currentSectionName, currentContent.toString().trim(), order));
        }

        return sections.stream()
                .filter(section -> section.getContent() != null && !section.getContent().isBlank())
                .toList();
    }

    private boolean isSectionHeading(XWPFParagraph paragraph, String text) {
        String style = paragraph.getStyle();
        if (style != null && style.toLowerCase(Locale.ROOT).contains("heading")) {
            return true;
        }

        String styleId = paragraph.getStyleID();
        if (styleId != null && styleId.toLowerCase(Locale.ROOT).contains("heading")) {
            return true;
        }

        String normalizedText = normalizeHeading(text).toLowerCase(Locale.ROOT);
        if (COMMON_SECTION_HEADINGS.contains(normalizedText)) {
            return true;
        }

        if (text.matches("^\\d+(\\.\\d+)*\\s+.+")) {
            return true;
        }

        if (text.matches("^[IVXLCDM]+\\.\\s+.+")) {
            return true;
        }

        return text.length() <= 80
                && text.equals(text.toUpperCase(Locale.ROOT))
                && text.matches(".*[A-Z].*");
    }

    private String normalizeHeading(String rawHeading) {
        String heading = rawHeading
                .replaceFirst("^\\d+(\\.\\d+)*\\s*", "")
                .replaceFirst("^[IVXLCDM]+\\.\\s*", "")
                .trim();

        if (heading.isEmpty()) {
            return rawHeading.trim();
        }

        return heading;
    }
}
