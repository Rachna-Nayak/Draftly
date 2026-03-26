package com.draftly.service;

import com.draftly.model.PaperSection;
import com.draftly.model.Reference;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * IeeeLatexFormatBuilder - Implementation of LatexFormatBuilder for IEEE format.
 * Encapsulates IEEE-specific LaTeX generation logic.
 * Follows Single Responsibility: only handles IEEE format specifics.
 */
@Component
public class IeeeLatexFormatBuilder implements LatexFormatBuilder {

    private final LatexEscapeUtils escapeUtils;

    public IeeeLatexFormatBuilder(LatexEscapeUtils escapeUtils) {
        this.escapeUtils = escapeUtils;
    }

    @Override
    public String getFormat() {
        return "IEEE";
    }

    @Override
    public String buildAuthorsBlock() {
        // IEEE requires explicit author block
        return "\\author{Draftly Author}";
    }

    @Override
    public String buildSections(List<PaperSection> sections, Map<Integer, String> citationKeyMap) {
        StringBuilder sectionsLatex = new StringBuilder();
        
        for (PaperSection section : sections) {
            if (section.getSectionName() == null) {
                continue;
            }

            String normalizedSectionName = section.getSectionName().trim().toLowerCase(Locale.ROOT);
            // Skip abstract and keywords as they are handled separately in IEEE format
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
        return "\\begin{IEEEkeywords}\n" + keywords + "\n\\end{IEEEkeywords}";
    }

    private String replaceNumberedCitations(String text, Map<Integer, String> citationKeyMap) {
        if (text == null || text.isBlank() || citationKeyMap.isEmpty()) {
            return text == null ? "" : text;
        }
        
        return text
                .replaceAll("\\[(\\d+)]", "\\\\cite{$1}")
                .replaceAll("\\[(\\d+(?:\\s*[-,]\\s*\\d+)*)\\]", "\\\\cite{$1}");
    }
}
