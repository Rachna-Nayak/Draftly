package com.draftly.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * ClassPathLatexTemplateProvider - Implementation of LatexTemplateProvider that loads templates
 * from the classpath (resources/templates/latex/).
 * Follows Single Responsibility Principle by handling only template loading logic.
 */
@Component
public class ClassPathLatexTemplateProvider implements LatexTemplateProvider {

    /**
     * Load the LaTeX template for the given format from the classpath.
     *
     * @param format The format name (e.g., "IEEE", "LNCS")
     * @return The LaTeX template content as a string
     * @throws IllegalStateException if template cannot be loaded
     */
    @Override
    public String loadTemplate(String format) {
        String normalizedFormat = normalizeFormat(format);
        String resourcePath = buildResourcePath(normalizedFormat);

        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            // Fallback to default template if file not found
            return getDefaultTemplate(normalizedFormat);
        }
    }

    /**
     * Normalize the format name to uppercase.
     */
    private String normalizeFormat(String format) {
        if (format == null || format.isBlank()) {
            return "IEEE";
        }
        return format.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Build the resource path based on format.
     */
    private String buildResourcePath(String normalizedFormat) {
        return "LNCS".equals(normalizedFormat)
                ? "templates/latex/lncs_template.tex"
                : "templates/latex/ieee_template.tex";
    }

    /**
     * Provide default templates as fallback if file loading fails.
     * Follows Robustness principle - handles missing files gracefully.
     */
    private String getDefaultTemplate(String format) {
        if ("LNCS".equals(format)) {
            return getDefaultLncsTemplate();
        }
        return getDefaultIeeeTemplate();
    }

    private String getDefaultIeeeTemplate() {
        return """
                \\documentclass[conference]{IEEEtran}
                \\usepackage{cite}
                \\usepackage{amsmath,amssymb,amsfonts}
                \\usepackage{graphicx}

                \\begin{document}
                \\title{{{PAPER_TITLE}}}
                {{PAPER_AUTHORS}}
                \\maketitle

                \\begin{abstract}
                {{PAPER_ABSTRACT}}
                \\end{abstract}

                \\begin{IEEEkeywords}
                {{PAPER_KEYWORDS}}
                \\end{IEEEkeywords}

                {{PAPER_SECTIONS}}

                \\bibliography{references}
                \\bibliographystyle{IEEEtran}
                \\end{document}
                """;
    }

    private String getDefaultLncsTemplate() {
        return """
                \\documentclass[runningheads]{llncs}
                \\usepackage{graphicx}

                \\begin{document}
                \\title{{{PAPER_TITLE}}}
                \\author{Draftly Author}
                \\institute{Institution}
                \\maketitle

                \\begin{abstract}
                {{PAPER_ABSTRACT}}
                \\keywords{{PAPER_KEYWORDS}}
                \\end{abstract}

                {{PAPER_SECTIONS}}

                \\bibliography{references}
                \\bibliographystyle{splncs04}
                \\end{document}
                """;
    }
}
