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
     * Input examples supported:
     * [1] @article{key1, ...}
     * [2] @inproceedings{key2, ...}
     * or 1. @article{key1, ...}
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
