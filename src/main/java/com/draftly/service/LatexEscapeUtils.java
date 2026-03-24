package com.draftly.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LatexEscapeUtils - Utility class for LaTeX escaping operations.
 * Follows Single Responsibility: only handles LaTeX character escaping logic.
 * This is a utility/helper component that both format builders can reuse.
 */
@Component
public class LatexEscapeUtils {

    private static final Pattern CITE_COMMAND_PATTERN = Pattern.compile("\\\\cite\\{[^}]+}");

    /**
     * Escape special LaTeX characters in text while preserving \cite{} commands.
     * This is crucial for academic content that contains citations.
     */
    public String escapeLatexPreservingCitations(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        Matcher matcher = CITE_COMMAND_PATTERN.matcher(text);

        String transformed = text;
        List<String> tokens = new ArrayList<>();
        List<String> commands = new ArrayList<>();

        // Replace all \cite{} commands with temporary tokens
        while (matcher.find()) {
            String command = matcher.group();
            String token = "CITECMDTOKEN" + tokens.size() + "END";
            transformed = transformed.replace(command, token);
            tokens.add(token);
            commands.add(command);
        }

        // Escape the text (without citations)
        String escaped = escapeLatex(transformed);
        
        // Restore \cite{} commands
        for (int index = 0; index < tokens.size(); index++) {
            escaped = escaped.replace(tokens.get(index), commands.get(index));
        }

        return escaped;
    }

    /**
     * Escape special LaTeX characters in text.
     * Order matters: backslash must be escaped first!
     */
    public String escapeLatex(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\textbackslash{}")
                .replace("&", "\\&")
                .replace("%", "\\%")
                .replace("$", "\\$")
                .replace("#", "\\#")
                .replace("_", "\\_")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("~", "\\textasciitilde{}")
                .replace("^", "\\textasciicircum{}");
    }

    /**
     * Escape special characters for BibTeX values.
     */
    public String escapeBibtexValue(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (char character : value.toCharArray()) {
            switch (character) {
                case '\\' -> escaped.append("\\textbackslash{}");
                case '{' -> escaped.append("\\{");
                case '}' -> escaped.append("\\}");
                case '&' -> escaped.append("\\&");
                case '%' -> escaped.append("\\%");
                case '$' -> escaped.append("\\$");
                case '#' -> escaped.append("\\#");
                case '_' -> escaped.append("\\_");
                case '~' -> escaped.append("\\textasciitilde{}");
                case '^' -> escaped.append("\\textasciicircum{}");
                case '\n', '\r' -> escaped.append(' ');
                default -> escaped.append(character);
            }
        }

        return escaped.toString().replaceAll("\\s{2,}", " ").trim();
    }
}
