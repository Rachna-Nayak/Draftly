package com.draftly.service;

/**
 * LatexTemplateProvider - Responsible for loading LaTeX templates for different formats.
 * Follows Single Responsibility Principle by separating template management from export logic.
 */
public interface LatexTemplateProvider {
    /**
     * Load the LaTeX template for the given format.
     *
     * @param format The format name (e.g., "IEEE", "LNCS")
     * @return The LaTeX template content as a string
     */
    String loadTemplate(String format);
}
