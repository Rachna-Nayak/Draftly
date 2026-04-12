package com.draftly.service;

import java.util.List;
import java.util.Map;

import com.draftly.model.PaperSection;

/**
 * LatexFormatBuilder - Interface for format-specific LaTeX generation.
 * Follows Open/Closed Principle: open for extension (new formats) without modifying existing code.
 * Different formats (IEEE, LNCS, etc.) can implement this interface with their own specific logic.
 */
public interface LatexFormatBuilder {
    /**
     * Get the format name this builder handles.
     */
    String getFormat();

    /**
     * Build the authors block for this format.
     */
    String buildAuthorsBlock();

    /**
     * Build the sections for this format.
     */
    String buildSections(List<PaperSection> sections, Map<Integer, String> citationKeyMap);

    /**
     * Build the keywords section for this format.
     */
    String buildKeywords(String keywords);
}
