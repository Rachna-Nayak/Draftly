package com.draftly.service;

import com.draftly.model.Paper;
import com.draftly.model.Reference;
import com.draftly.model.ResearchProject;
import com.draftly.repository.PaperRepository;
import com.draftly.repository.ProjectRepository;
import com.draftly.repository.ReferenceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reference Service - Manages references and citation generation. (UC4)
 */
@Service
public class ReferenceService {

    private static final Pattern NUMBERED_REFERENCE_MARKER =
        Pattern.compile("(?m)^\\s*(?:\\[(\\d+)]|(\\d+)\\.)\\s*(?=@)");
    private static final Pattern BIBTEX_KEY_PATTERN =
        Pattern.compile("@\\w+\\s*\\{\\s*([^,\\s]+)\\s*,", Pattern.CASE_INSENSITIVE);

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
     * FR6: Save numbered BibTeX references uploaded with a paper.
     */
    public List<Reference> saveNumberedBibtexReferences(String projectId, String numberedBibtex) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("projectId is required to save BibTeX references.");
        }
        if (numberedBibtex == null || numberedBibtex.isBlank()) {
            return List.of();
        }

        List<ParsedBibtexEntry> parsedEntries = parseNumberedBibtexEntries(numberedBibtex);
        if (parsedEntries.isEmpty()) {
            throw new IllegalArgumentException("No valid BibTeX entries were found in the uploaded references.");
        }

        referenceRepository.deleteByProjectIdAndCitationFormat(projectId, "BIBTEX");

        List<Reference> savedReferences = new ArrayList<>();
        for (ParsedBibtexEntry entry : parsedEntries) {
            Reference reference = new Reference(projectId, entry.bibtexKey(), "BIBTEX", entry.bibtexEntry());
            reference.setReferenceNumber(entry.referenceNumber());
            reference.setBibtexKey(entry.bibtexKey());
            savedReferences.add(referenceRepository.save(reference));
        }

        return savedReferences;
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

    /**
     * UC4: Export all references for a project as BibTeX format.
     */
    public String exportAsBibtex(String projectId) {
        List<Reference> references = getReferencesByProject(projectId);
        if (references.isEmpty()) {
            return "% No references found for this project\n";
        }

        StringBuilder bibtex = new StringBuilder();
        bibtex.append("% BibTeX references exported from Draftly\n");
        bibtex.append("% Project ID: ").append(projectId).append("\n\n");

        for (Reference ref : references) {
            if ("BIBTEX".equals(ref.getCitationFormat())) {
                bibtex.append(ref.getFormattedCitation()).append("\n\n");
            } else if (ref.getPaperId() != null && !ref.getPaperId().isBlank()) {
                Optional<Paper> paperOpt = paperRepository.findById(ref.getPaperId());
                if (paperOpt.isPresent()) {
                    bibtex.append(convertPaperToBibtex(paperOpt.get(), ref.getBibtexKey() != null ? ref.getBibtexKey() : "ref" + ref.getId().hashCode()))
                            .append("\n\n");
                }
            }
        }

        return bibtex.toString();
    }

    /**
     * UC4: Export all references for a project as plain text with chosen citation format.
     */
    public String exportAsPlaintext(String projectId, String format) {
        List<Reference> references = getReferencesByProject(projectId);
        if (references.isEmpty()) {
            return "No references found for this project.\n";
        }

        StringBuilder plaintext = new StringBuilder();
        plaintext.append("References for Project: ").append(projectId).append("\n");
        plaintext.append("Citation Format: ").append(format.toUpperCase()).append("\n");
        plaintext.append("=".repeat(80)).append("\n\n");

        int index = 1;
        for (Reference ref : references) {
            plaintext.append("[").append(index).append("] ");

            if ("BIBTEX".equals(ref.getCitationFormat())) {
                plaintext.append(ref.getFormattedCitation());
            } else if (ref.getPaperId() != null && !ref.getPaperId().isBlank()) {
                Optional<Paper> paperOpt = paperRepository.findById(ref.getPaperId());
                if (paperOpt.isPresent()) {
                    plaintext.append(generateCitation(paperOpt.get(), format));
                }
            } else {
                plaintext.append(ref.getFormattedCitation());
            }

            plaintext.append("\n\n");
            index++;
        }

        return plaintext.toString();
    }

    /**
     * UC4: Export all references for a project as HTML with chosen citation format.
     */
    public String exportAsHtml(String projectId, String format) {
        List<Reference> references = getReferencesByProject(projectId);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Project References - ").append(projectId).append("</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; line-height: 1.6; margin: 2rem; background-color: #f5f5f5; }\n");
        html.append("        .container { max-width: 900px; margin: 0 auto; background-color: white; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 0.5rem; }\n");
        html.append("        .meta { color: #666; font-size: 0.9rem; margin-bottom: 2rem; }\n");
        html.append("        .reference { margin-bottom: 1.5rem; padding: 1rem; background-color: #f9f9f9; border-left: 4px solid #007bff; border-radius: 4px; }\n");
        html.append("        .reference-number { font-weight: bold; color: #007bff; margin-right: 0.5rem; }\n");
        html.append("        .reference-text { color: #333; }\n");
        html.append("        .empty { color: #999; font-style: italic; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>📚 Project References</h1>\n");
        html.append("        <div class=\"meta\">\n");
        html.append("            <p><strong>Project ID:</strong> ").append(projectId).append("</p>\n");
        html.append("            <p><strong>Citation Format:</strong> ").append(format.toUpperCase()).append("</p>\n");
        html.append("            <p><strong>Total References:</strong> ").append(references.size()).append("</p>\n");
        html.append("        </div>\n");

        if (references.isEmpty()) {
            html.append("        <p class=\"empty\">No references found for this project.</p>\n");
        } else {
            int index = 1;
            for (Reference ref : references) {
                html.append("        <div class=\"reference\">\n");
                html.append("            <span class=\"reference-number\">[").append(index).append("]</span>\n");
                html.append("            <span class=\"reference-text\">");

                if ("BIBTEX".equals(ref.getCitationFormat())) {
                    html.append(escapeHtml(ref.getFormattedCitation()));
                } else if (ref.getPaperId() != null && !ref.getPaperId().isBlank()) {
                    Optional<Paper> paperOpt = paperRepository.findById(ref.getPaperId());
                    if (paperOpt.isPresent()) {
                        html.append(escapeHtml(generateCitation(paperOpt.get(), format)));
                    }
                } else {
                    html.append(escapeHtml(ref.getFormattedCitation()));
                }

                html.append("</span>\n");
                html.append("        </div>\n");
                index++;
            }
        }

        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    /**
     * Convert a Paper object to BibTeX format.
     */
    private String convertPaperToBibtex(Paper paper, String key) {
        StringBuilder bibtex = new StringBuilder();
        bibtex.append("@article{").append(key).append(",\n");
        bibtex.append("  author = \"").append(String.join(" and ", paper.getAuthors())).append("\",\n");
        bibtex.append("  title = \"").append(paper.getTitle()).append("\",\n");
        bibtex.append("  journal = \"").append(paper.getJournal()).append("\",\n");
        bibtex.append("  year = ").append(paper.getPublicationYear()).append(",\n");

        if (paper.getDoi() != null && !paper.getDoi().isBlank()) {
            bibtex.append("  doi = \"").append(paper.getDoi()).append("\",\n");
        }

        bibtex.append("  keywords = \"").append(String.join(", ", paper.getKeywords())).append("\"\n");
        bibtex.append("}");

        return bibtex.toString();
    }

    /**
     * Escape HTML special characters for safe display in HTML.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private List<ParsedBibtexEntry> parseNumberedBibtexEntries(String numberedBibtex) {
        String normalized = numberedBibtex.replace("\r\n", "\n").replace("\r", "\n");
        Matcher markerMatcher = NUMBERED_REFERENCE_MARKER.matcher(normalized);
        List<ReferenceMarker> markers = new ArrayList<>();

        while (markerMatcher.find()) {
            String firstGroup = markerMatcher.group(1);
            String secondGroup = markerMatcher.group(2);
            int number = Integer.parseInt(firstGroup != null ? firstGroup : secondGroup);
            markers.add(new ReferenceMarker(number, markerMatcher.start()));
        }

        List<ParsedBibtexEntry> parsedEntries = new ArrayList<>();
        if (!markers.isEmpty()) {
            for (int index = 0; index < markers.size(); index++) {
                int start = markers.get(index).startIndex();
                int end = (index + 1 < markers.size()) ? markers.get(index + 1).startIndex() : normalized.length();
                String chunk = normalized.substring(start, end).trim();
                chunk = chunk.replaceFirst("^\\s*(?:\\[\\d+]|\\d+\\.)\\s*", "").trim();

                List<String> blocks = extractBibtexBlocks(chunk);
                if (blocks.isEmpty()) {
                    throw new IllegalArgumentException("Invalid numbered BibTeX list. Entry [" + markers.get(index).referenceNumber() + "] is malformed.");
                }

                String bibtexEntry = blocks.getFirst();
                String bibtexKey = extractBibtexKey(bibtexEntry, markers.get(index).referenceNumber());
                parsedEntries.add(new ParsedBibtexEntry(markers.get(index).referenceNumber(), bibtexKey, bibtexEntry));
            }

            return parsedEntries;
        }

        List<String> blocks = extractBibtexBlocks(normalized);
        int number = 1;
        for (String block : blocks) {
            String bibtexKey = extractBibtexKey(block, number);
            parsedEntries.add(new ParsedBibtexEntry(number, bibtexKey, block));
            number++;
        }

        return parsedEntries;
    }

    private List<String> extractBibtexBlocks(String input) {
        List<String> entries = new ArrayList<>();
        int cursor = 0;

        while (cursor < input.length()) {
            int atIndex = input.indexOf('@', cursor);
            if (atIndex < 0) {
                break;
            }

            int openingBrace = input.indexOf('{', atIndex);
            if (openingBrace < 0) {
                break;
            }

            int depth = 0;
            int closingBrace = -1;
            for (int position = openingBrace; position < input.length(); position++) {
                char character = input.charAt(position);
                if (character == '{') {
                    depth++;
                } else if (character == '}') {
                    depth--;
                    if (depth == 0) {
                        closingBrace = position;
                        break;
                    }
                }
            }

            if (closingBrace < 0) {
                throw new IllegalArgumentException("Malformed BibTeX entry detected. Check braces in uploaded references.");
            }

            String entry = input.substring(atIndex, closingBrace + 1).trim();
            entries.add(entry);
            cursor = closingBrace + 1;
        }

        return entries;
    }

    private String extractBibtexKey(String bibtexEntry, int fallbackNumber) {
        Matcher keyMatcher = BIBTEX_KEY_PATTERN.matcher(bibtexEntry);
        String key;
        if (keyMatcher.find()) {
            key = keyMatcher.group(1);
        } else {
            key = "ref" + fallbackNumber;
        }

        String sanitized = key
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9:_-]", "");
        if (sanitized.isBlank()) {
            sanitized = "ref" + fallbackNumber;
        }
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "ref" + sanitized;
        }
        return sanitized;
    }

    private record ReferenceMarker(int referenceNumber, int startIndex) {}
    private record ParsedBibtexEntry(int referenceNumber, String bibtexKey, String bibtexEntry) {}
}
