package com.draftly.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.draftly.model.PaperSection;
import com.draftly.model.PlagiarismReport;
import com.draftly.model.Reference;
import com.draftly.model.ResearchPaper;
import com.draftly.repository.ResearchPaperRepository;

/**
 * Export Service - Validates formatting and exports papers. (UC8)
 * 
 * This service orchestrates the export process by delegating format-specific logic
 * to specialized components (LatexTemplateProvider, LatexFormatBuilder, LatexEscapeUtils).
 * Follows Single Responsibility and Dependency Inversion principles.
 */
@Service
public class ExportService {

    private static final Pattern NUMBERED_CITATION_PATTERN =
        Pattern.compile("\\[(\\d+(?:\\s*[-,]\\s*\\d+)*)]");
    private static final Pattern BIBTEX_HEADER_PATTERN =
        Pattern.compile("@(\\w+)\\s*\\{\\s*([^,\\s]+)\\s*,", Pattern.CASE_INSENSITIVE);

    private final ResearchPaperRepository researchPaperRepository;
    private final ReferenceService referenceService;
    private final PlagiarismService plagiarismService;
    private final NLPService nlpService;
    private final LatexTemplateProvider templateProvider;
    private final Map<String, LatexFormatBuilder> formatBuilders;
    private final LatexEscapeUtils escapeUtils;

    public ExportService(ResearchPaperRepository researchPaperRepository,
                         ReferenceService referenceService,
                         PlagiarismService plagiarismService,
                         NLPService nlpService,
                         LatexTemplateProvider templateProvider,
                         IeeeLatexFormatBuilder ieeeBuilder,
                         LncsLatexFormatBuilder lncsBuilder,
                         LatexEscapeUtils escapeUtils) {
        this.researchPaperRepository = researchPaperRepository;
        this.referenceService = referenceService;
        this.plagiarismService = plagiarismService;
        this.nlpService = nlpService;
        this.templateProvider = templateProvider;
        this.escapeUtils = escapeUtils;
        this.formatBuilders = new HashMap<>();
        this.formatBuilders.put(ieeeBuilder.getFormat(), ieeeBuilder);
        this.formatBuilders.put(lncsBuilder.getFormat(), lncsBuilder);
    }

    /*
     UC8: Run a submission readiness check on the paper.
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

    /*
     UC8: Export paper as PDF.
     */
    public byte[] exportAsPDF(String researchPaperId) {
        ResearchPaper paper = researchPaperRepository.findById(researchPaperId)
                .orElseThrow(() -> new IllegalArgumentException("Research paper not found: " + researchPaperId));

        String latex = exportAsLatex(researchPaperId);
        String bib = generateBib(paper.getProjectId());

        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("draftly-latex-");
            Files.writeString(workDir.resolve("main.tex"), latex, StandardCharsets.UTF_8);
            Files.writeString(workDir.resolve("references.bib"), bib == null ? "" : bib, StandardCharsets.UTF_8);

            compileLatexProject(workDir);

            Path pdfPath = workDir.resolve("main.pdf");
            if (!Files.exists(pdfPath)) {
                throw new IllegalStateException("PDF generation failed: compiler completed but no PDF file was produced.");
            }

            return Files.readAllBytes(pdfPath);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("PDF generation was interrupted.", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("PDF generation failed: " + exception.getMessage(), exception);
        } finally {
            deleteDirectoryQuietly(workDir);
        }
    }

    /*
     UC8: Export paper as Word document.
     */
    public byte[] exportAsWord(String researchPaperId) {
        // TODO: Implement Word export using Apache POI
        return new byte[0];
    }

    /*
     UC8: Export paper as LaTeX.
      
     * This method orchestrates the LaTeX export process:
     * 1. Load the appropriate template via LatexTemplateProvider
     * 2. Get the format-specific builder for handling format-specific logic
     * 3. Build sections, authors, and keywords using the format builder
     * 4. Replace placeholders in the template
     */
    public String exportAsLatex(String researchPaperId) {
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(researchPaperId);
        if (paperOpt.isEmpty()) {
            throw new IllegalArgumentException("Research paper not found: " + researchPaperId);
        }

        ResearchPaper paper = paperOpt.get();
        String templateFormat = normalizeTemplateName(paper.getTemplate());
        
        // Get format-specific builder
        LatexFormatBuilder formatBuilder = formatBuilders.getOrDefault(templateFormat, formatBuilders.get("IEEE"));
        
        // Prepare data
        List<Reference> projectReferences = referenceService.getReferencesByProject(paper.getProjectId());
        Map<Integer, String> citationKeyMap = buildCitationKeyMap(projectReferences);
        List<PaperSection> orderedSections = paper.getSections().stream()
                .sorted(Comparator.comparingInt(PaperSection::getOrder))
                .toList();

        // Extract and prepare content
        String abstractText = escapeUtils.escapeLatexPreservingCitations(
            replaceNumberedCitations(extractSectionContent(orderedSections, "abstract"), citationKeyMap)
        );
        String keywordText = escapeUtils.escapeLatex(extractSectionContent(orderedSections, "keywords"));
        String renderedSections = formatBuilder.buildSections(orderedSections, citationKeyMap);
        String escapedTitle = escapeUtils.escapeLatex(paper.getTitle());
        String authorsBlock = formatBuilder.buildAuthorsBlock();

        // Load template and replace placeholders
        String latexTemplate = templateProvider.loadTemplate(templateFormat);
        return latexTemplate
            .replace("{{{PAPER_TITLE}}}", escapedTitle)
            .replace("{{PAPER_TITLE}}", escapedTitle)
            .replace("{{PAPER_AUTHORS}}", authorsBlock)
            .replace("{{PAPER_ABSTRACT}}", abstractText)
            .replace("{{PAPER_KEYWORDS}}", formatBuilder.buildKeywords(keywordText))
            .replace("{{PAPER_SECTIONS}}", renderedSections);
    }

    /**
     * UC8: Generate references.bib content for a paper's project.
     */
    public String exportBibForPaper(String researchPaperId) {
        ResearchPaper paper = researchPaperRepository.findById(researchPaperId)
            .orElseThrow(() -> new IllegalArgumentException("Research paper not found: " + researchPaperId));
        return generateBib(paper.getProjectId());
    }

    /**
     * UC8: Generate references.bib content for a project.
     */
    public String generateBib(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("projectId is required to generate references.bib");
        }

        List<Reference> references = referenceService.getReferencesByProject(projectId);
        if (references == null || references.isEmpty()) {
            return "";
        }

        List<Reference> orderedReferences = references.stream()
                .sorted(referenceComparator())
                .toList();

        StringBuilder bibBuilder = new StringBuilder();
        Set<String> usedKeys = new HashSet<>();
        int fallbackIndex = 1;

        for (Reference reference : orderedReferences) {
            String baseKey = resolveReferenceKey(reference, fallbackIndex++);
            String key = uniqueKey(baseKey, usedKeys);

            if (isBibtexReference(reference)) {
                String rewrittenEntry = rewriteBibtexEntryKey(reference.getFormattedCitation(), key);
                bibBuilder.append(rewrittenEntry).append("\n\n");
                continue;
            }

            String formattedCitation = reference.getFormattedCitation();
            if (formattedCitation == null || formattedCitation.isBlank()) {
                formattedCitation = "Reference " + key;
            }

            bibBuilder.append("@misc{").append(key).append(",\n");
            bibBuilder.append("  note = {").append(escapeUtils.escapeBibtexValue(formattedCitation)).append("}");
            if (reference.getCitationFormat() != null && !reference.getCitationFormat().isBlank()) {
                bibBuilder.append(",\n  annote = {Citation Format: ")
                        .append(escapeUtils.escapeBibtexValue(reference.getCitationFormat().toUpperCase(Locale.ROOT)))
                        .append("}");
            }
            bibBuilder.append("\n}\n\n");
        }

        return bibBuilder.toString().trim();
    }

    private void compileLatexProject(Path workDir) throws IOException, InterruptedException {
        List<String> failures = new ArrayList<>();

        try {
            runCommand(workDir, List.of(
                    "latexmk", "-pdf", "-interaction=nonstopmode", "-halt-on-error", "-file-line-error", "main.tex"
            ), 120);
            return;
        } catch (IOException exception) {
            failures.add("latexmk: " + exception.getMessage());
        }

        try {
            runCommand(workDir, List.of(
                    "pdflatex", "-interaction=nonstopmode", "-halt-on-error", "-file-line-error", "main.tex"
            ), 120);
            runCommand(workDir, List.of("bibtex", "main"), 120);
            runCommand(workDir, List.of(
                    "pdflatex", "-interaction=nonstopmode", "-halt-on-error", "-file-line-error", "main.tex"
            ), 120);
            runCommand(workDir, List.of(
                    "pdflatex", "-interaction=nonstopmode", "-halt-on-error", "-file-line-error", "main.tex"
            ), 120);
            return;
        } catch (IOException exception) {
            failures.add("pdflatex/bibtex: " + exception.getMessage());
        }

        throw new IllegalStateException(
                "No working LaTeX PDF compiler was available. Install latexmk or pdflatex+bibtex. Details: "
                        + String.join(" | ", failures)
        );
    }

    private void runCommand(Path workDir, List<String> command, long timeoutSeconds)
            throws IOException, InterruptedException {
        Path logFile = Files.createTempFile(workDir, "latex-command-", ".log");
        Process process;
        try {
            process = new ProcessBuilder(command)
                    .directory(workDir.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(logFile.toFile())
                    .start();
        } catch (IOException exception) {
            throw new IOException("Could not start command '" + String.join(" ", command) + "'. " + exception.getMessage(), exception);
        }

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        String output = Files.exists(logFile) ? Files.readString(logFile, StandardCharsets.UTF_8) : "";

        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Command timed out after " + timeoutSeconds + " seconds: "
                    + String.join(" ", command) + "\n" + truncateOutput(output));
        }

        if (process.exitValue() != 0) {
            throw new IOException("Command failed: " + String.join(" ", command)
                    + "\n" + truncateOutput(output));
        }
    }

    private String truncateOutput(String output) {
        if (output == null || output.isBlank()) {
            return "";
        }

        int maxLength = 4000;
        if (output.length() <= maxLength) {
            return output;
        }

        return output.substring(0, maxLength) + "\n...[truncated]";
    }

    private void deleteDirectoryQuietly(Path directory) {
        if (directory == null) {
            return;
        }

        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Best-effort temp cleanup.
                        }
                    });
        } catch (IOException ignored) {
            // Best-effort temp cleanup.
        }
    }

    private String normalizeTemplateName(String template) {
        if (template == null || template.isBlank()) {
            return "IEEE";
        }

        String normalized = template.trim().toUpperCase(Locale.ROOT);
        if ("IEEE".equals(normalized) || "LNCS".equals(normalized)) {
            return normalized;
        }

        return "IEEE";
    }

    private String extractSectionContent(List<PaperSection> sections, String sectionName) {
        return sections.stream()
                .filter(section -> section.getSectionName() != null)
                .filter(section -> section.getSectionName().trim().equalsIgnoreCase(sectionName))
                .map(PaperSection::getContent)
                .filter(content -> content != null && !content.isBlank())
                .findFirst()
                .orElse(sectionName.equalsIgnoreCase("keywords")
                        ? "research, paper"
                        : "No abstract available.");
    }

    private Map<Integer, String> buildCitationKeyMap(List<Reference> references) {
        Map<Integer, String> citationKeyMap = new HashMap<>();
        List<Reference> orderedReferences = references.stream()
                .sorted(referenceComparator())
                .toList();

        int nextNumber = 1;
        int fallbackIndex = 1;
        for (Reference reference : orderedReferences) {
            Integer referenceNumber = reference.getReferenceNumber();
            if (referenceNumber == null || referenceNumber <= 0) {
                while (citationKeyMap.containsKey(nextNumber)) {
                    nextNumber++;
                }
                referenceNumber = nextNumber;
            }

            String referenceKey = resolveReferenceKey(reference, fallbackIndex++);
            citationKeyMap.putIfAbsent(referenceNumber, referenceKey);
        }

        return citationKeyMap;
    }

    private Comparator<Reference> referenceComparator() {
        return Comparator
                .comparing((Reference reference) ->
                        reference.getReferenceNumber() == null ? Integer.MAX_VALUE : reference.getReferenceNumber())
                .thenComparing(reference -> reference.getId() == null ? "" : reference.getId());
    }

    private String resolveReferenceKey(Reference reference, int fallbackIndex) {
        String source = reference.getBibtexKey();
        if (source == null || source.isBlank()) {
            source = reference.getPaperId();
        }
        if (source == null || source.isBlank()) {
            source = reference.getId();
        }
        if (source == null || source.isBlank()) {
            source = "reference" + fallbackIndex;
        }

        String sanitized = source.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
        if (sanitized.isBlank()) {
            sanitized = "reference" + fallbackIndex;
        }
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "ref" + sanitized;
        }

        return sanitized;
    }

    private String uniqueKey(String baseKey, Set<String> usedKeys) {
        String candidate = baseKey;
        int duplicateSuffix = 2;
        while (!usedKeys.add(candidate)) {
            candidate = baseKey + duplicateSuffix;
            duplicateSuffix++;
        }
        return candidate;
    }

    private boolean isBibtexReference(Reference reference) {
        return reference.getCitationFormat() != null
                && "BIBTEX".equalsIgnoreCase(reference.getCitationFormat())
                && reference.getFormattedCitation() != null
                && reference.getFormattedCitation().trim().startsWith("@");
    }

    private String rewriteBibtexEntryKey(String bibtexEntry, String key) {
        if (bibtexEntry == null || bibtexEntry.isBlank()) {
            return "@misc{" + key + ",\n  note = {Missing BibTeX entry}\n}";
        }

        Matcher matcher = BIBTEX_HEADER_PATTERN.matcher(bibtexEntry.trim());
        if (!matcher.find()) {
            return "@misc{" + key + ",\n  note = {" + escapeUtils.escapeBibtexValue(bibtexEntry) + "}\n}";
        }

        String entryType = matcher.group(1).toLowerCase(Locale.ROOT);
        String rewrittenHeader = "@" + entryType + "{" + key + ",";
        String entry = bibtexEntry.trim();
        return rewrittenHeader + entry.substring(matcher.end());
    }

    private String replaceNumberedCitations(String text, Map<Integer, String> citationKeyMap) {
        if (text == null || text.isBlank()) {
            return "";
        }
        if (citationKeyMap.isEmpty()) {
            return text;
        }

        Matcher matcher = NUMBERED_CITATION_PATTERN.matcher(text);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            List<Integer> numbers = parseCitationNumbers(matcher.group(1));
            LinkedHashSet<String> keys = new LinkedHashSet<>();

            boolean unresolved = numbers.isEmpty();
            for (Integer number : numbers) {
                String key = citationKeyMap.get(number);
                if (key == null || key.isBlank()) {
                    unresolved = true;
                    break;
                }
                keys.add(key);
            }

            String replacement = unresolved
                    ? matcher.group()
                    : "\\cite{" + String.join(",", keys) + "}";
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private List<Integer> parseCitationNumbers(String citationGroup) {
        List<Integer> numbers = new ArrayList<>();
        if (citationGroup == null || citationGroup.isBlank()) {
            return numbers;
        }

        String[] parts = citationGroup.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (trimmed.contains("-")) {
                String[] bounds = trimmed.split("-");
                if (bounds.length != 2) {
                    return List.of();
                }

                Integer start = parsePositiveInt(bounds[0].trim());
                Integer end = parsePositiveInt(bounds[1].trim());
                if (start == null || end == null || end < start || end - start > 100) {
                    return List.of();
                }

                for (int value = start; value <= end; value++) {
                    numbers.add(value);
                }
                continue;
            }

            Integer value = parsePositiveInt(trimmed);
            if (value == null) {
                return List.of();
            }
            numbers.add(value);
        }

        return numbers;
    }

    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /*
     Inner class representing the readiness report for export.
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
