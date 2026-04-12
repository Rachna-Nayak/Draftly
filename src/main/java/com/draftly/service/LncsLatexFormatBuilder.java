package com.draftly.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.draftly.model.PaperSection;

/**
 * LncsLatexFormatBuilder - Implementation of LatexFormatBuilder for LNCS (Springer) format.
 * Encapsulates LNCS-specific LaTeX generation logic.
 * Follows Single Responsibility: only handles LNCS format specifics.
 */
@Component
public class LncsLatexFormatBuilder implements LatexFormatBuilder {

    private static final Pattern NUMBERED_CITATION_PATTERN =
            Pattern.compile("\\[(\\d+(?:\\s*[-,]\\s*\\d+)*)]");

    private final LatexEscapeUtils escapeUtils;

    public LncsLatexFormatBuilder(LatexEscapeUtils escapeUtils) {
        this.escapeUtils = escapeUtils;
    }

    @Override
    public String getFormat() {
        return "LNCS";
    }

    @Override
    public String buildAuthorsBlock() {
        // LNCS format doesn't include author block in the same way as IEEE
        // Authors are typically defined in the \author{} and \institute{} commands
        return "";
    }

    @Override
    public String buildSections(List<PaperSection> sections, Map<Integer, String> citationKeyMap) {
        StringBuilder sectionsLatex = new StringBuilder();
        
        for (PaperSection section : sections) {
            if (section.getSectionName() == null) {
                continue;
            }

            String normalizedSectionName = section.getSectionName().trim().toLowerCase(Locale.ROOT);
            // Skip abstract and keywords as they are handled separately in LNCS format
            if ("abstract".equals(normalizedSectionName) || "keywords".equals(normalizedSectionName)) {
                continue;
            }

            String sectionContent = section.getContent() == null ? "" : section.getContent();
            String sectionWithLatexCitations = replaceNumberedCitations(sectionContent, citationKeyMap);
            
            sectionsLatex
                    .append("\\section{")
                    .append(escapeUtils.escapeLatex(section.getSectionName()))
                    .append("}\n")
                    .append(escapeUtils.escapeLatexPreservingCitations(sectionWithLatexCitations))
                    .append("\n\n");
        }

        return sectionsLatex.toString().trim();
    }

    @Override
    public String buildKeywords(String keywords) {
        // LNCS uses \keywords command within abstract environment
        return "\\keywords{" + keywords + "}";
    }

    /**
     * Replace numbered citations [1], [1-3], [1,2,3] with LaTeX \cite commands.
     * This is format-agnostic and follows citation replacement logic.
     */
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
            String citationGroup = matcher.group(1);
            List<Integer> numbers = parseCitationNumbers(citationGroup);
            
            if (numbers.isEmpty()) {
                // Couldn't parse, keep original
                matcher.appendReplacement(output, Matcher.quoteReplacement(matcher.group()));
                continue;
            }

            StringBuilder keyBuilder = new StringBuilder();
            boolean allResolved = true;
            
            for (int i = 0; i < numbers.size(); i++) {
                Integer number = numbers.get(i);
                String key = citationKeyMap.get(number);
                
                if (key == null || key.isBlank()) {
                    allResolved = false;
                    break;
                }
                
                if (i > 0) {
                    keyBuilder.append(",");
                }
                keyBuilder.append(key);
            }

            String replacement = allResolved
                    ? "\\cite{" + keyBuilder.toString() + "}"
                    : matcher.group();
            
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(output);
        return output.toString();
    }

    private List<Integer> parseCitationNumbers(String citationGroup) {
        java.util.List<Integer> numbers = new java.util.ArrayList<>();
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
                    return java.util.List.of();
                }

                Integer start = parsePositiveInt(bounds[0].trim());
                Integer end = parsePositiveInt(bounds[1].trim());
                if (start == null || end == null || end < start || end - start > 100) {
                    return java.util.List.of();
                }

                for (int value = start; value <= end; value++) {
                    numbers.add(value);
                }
                continue;
            }

            Integer value = parsePositiveInt(trimmed);
            if (value == null) {
                return java.util.List.of();
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
}
