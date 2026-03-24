package com.draftly.cli;

import com.draftly.model.PaperSection;
import com.draftly.model.ResearchPaper;
import com.draftly.service.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Locale;

/**
 * LatexExportCLI - Command-line utility for testing LaTeX export functionality.
 * 
 * Usage: java -jar draftly.jar --input <docx-file> --format <IEEE|LNCS> --output <output-dir>
 * 
 * This class integrates with Spring Boot to use the full dependency injection context
 * while providing command-line testing capabilities.
 */
@Component
public class LatexExportCLI implements CommandLineRunner {

    private static final Set<String> COMMON_SECTION_HEADINGS = Set.of(
            "abstract", "introduction", "related work", "background", "methodology", "methods",
            "materials and methods", "experiments", "results", "discussion", "conclusion",
            "conclusions", "future work", "acknowledgment", "acknowledgements", "references", "keywords"
    );

    private final LatexTemplateProvider templateProvider;
    private final IeeeLatexFormatBuilder ieeeBuilder;
    private final LncsLatexFormatBuilder lncsBuilder;
    private final LatexEscapeUtils escapeUtils;

    public LatexExportCLI(LatexTemplateProvider templateProvider,
                         IeeeLatexFormatBuilder ieeeBuilder,
                         LncsLatexFormatBuilder lncsBuilder,
                         LatexEscapeUtils escapeUtils) {
        this.templateProvider = templateProvider;
        this.ieeeBuilder = ieeeBuilder;
        this.lncsBuilder = lncsBuilder;
        this.escapeUtils = escapeUtils;
    }

    @Override
    public void run(String... args) throws Exception {
        Map<String, String> arguments = parseArguments(args);

        if (!arguments.containsKey("input") || !arguments.containsKey("format")) {
            // Not a CLI invocation, just return
            return;
        }

        String inputPath = arguments.get("input");
        String format = arguments.get("format").toUpperCase(Locale.ROOT);
        String outputDir = arguments.getOrDefault("output", "./latex-output");
        String title = arguments.getOrDefault("title", "Research Paper");

        // Validate format
        if (!format.equals("IEEE") && !format.equals("LNCS")) {
            System.err.println("❌ Error: Format must be IEEE or LNCS");
            System.exit(1);
        }

        // Check input file exists
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.err.println("❌ Error: Input file not found: " + inputPath);
            System.exit(1);
        }

        if (!inputFile.getName().toLowerCase().endsWith(".docx")) {
            System.err.println("❌ Error: Input file must be a .docx file");
            System.exit(1);
        }

        try {
            System.out.println("�� Processing: " + inputPath);
            System.out.println("📋 Format: " + format);
            System.out.println("📁 Output directory: " + outputDir);
            System.out.println();

            // Create output directory
            Path outputPath = Paths.get(outputDir);
            Files.createDirectories(outputPath);

            // Read DOCX file
            byte[] docxBytes = Files.readAllBytes(inputFile.toPath());

            // Parse sections from DOCX
            List<PaperSection> sections = parseSectionsFromDocx(docxBytes);

            if (sections.isEmpty()) {
                System.err.println("❌ Error: No sections found in document. Make sure you have proper headings.");
                System.exit(1);
            }

            // Create ResearchPaper object
            ResearchPaper paper = new ResearchPaper("cli-project", title);
            paper.setTemplate(format);
            paper.setSections(sections);

            // Print parsed sections
            System.out.println("✅ Parsed " + sections.size() + " sections:");
            for (PaperSection section : sections) {
                System.out.println("   - " + section.getSectionName() + " (" + section.getContent().length() + " chars)");
            }
            System.out.println();

            // Generate LaTeX
            String latex = generateLatex(paper, format);

            // Write LaTeX to file
            Path texFile = outputPath.resolve("main.tex");
            Files.writeString(texFile, latex, StandardCharsets.UTF_8);

            System.out.println("✅ LaTeX file generated: " + texFile.toAbsolutePath());
            System.out.println();
            System.out.println("📝 First 500 characters of output:");
            System.out.println("─".repeat(80));
            String preview = latex.length() > 500 ? latex.substring(0, 500) + "\n..." : latex;
            System.out.println(preview);
            System.out.println("─".repeat(80));
            System.out.println();
            System.out.println("💡 Tip: To compile LaTeX to PDF, run:");
            System.out.println("   cd " + outputDir);
            System.out.println("   pdflatex -interaction=nonstopmode main.tex");
            System.out.println();

            System.exit(0);
        } catch (IOException e) {
            System.err.println("❌ Error reading file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Generate LaTeX from a ResearchPaper object.
     */
    private String generateLatex(ResearchPaper paper, String format) {
        Map<String, LatexFormatBuilder> builders = new HashMap<>();
        builders.put("IEEE", ieeeBuilder);
        builders.put("LNCS", lncsBuilder);

        LatexFormatBuilder builder = builders.get(format);
        
        // Sort sections by order
        List<PaperSection> orderedSections = paper.getSections().stream()
                .sorted(Comparator.comparingInt(PaperSection::getOrder))
                .toList();

        // Extract content
        String abstractText = escapeUtils.escapeLatex(
                extractSectionContent(orderedSections, "abstract")
        );
        String keywordText = escapeUtils.escapeLatex(
                extractSectionContent(orderedSections, "keywords")
        );
        String renderedSections = builder.buildSections(orderedSections, new HashMap<>());
        String escapedTitle = escapeUtils.escapeLatex(paper.getTitle());
        String authorsBlock = builder.buildAuthorsBlock();

        // Load template and replace placeholders
        String latexTemplate = templateProvider.loadTemplate(format);
        return latexTemplate
                .replace("{{{PAPER_TITLE}}}", escapedTitle)
                .replace("{{PAPER_TITLE}}", escapedTitle)
                .replace("{{PAPER_AUTHORS}}", authorsBlock)
                .replace("{{PAPER_ABSTRACT}}", abstractText)
                .replace("{{PAPER_KEYWORDS}}", builder.buildKeywords(keywordText))
                .replace("{{PAPER_SECTIONS}}", renderedSections);
    }

    /**
     * Parse sections from DOCX file.
     */
    private static List<PaperSection> parseSectionsFromDocx(byte[] docxBytes) throws IOException {
        List<PaperSection> sections = new ArrayList<>();
        String currentSectionName = null;
        StringBuilder currentContent = new StringBuilder();
        int order = 1;

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
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
        }

        return sections.stream()
                .filter(section -> section.getContent() != null && !section.getContent().isBlank())
                .toList();
    }

    private static boolean isSectionHeading(XWPFParagraph paragraph, String text) {
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

    private static String normalizeHeading(String rawHeading) {
        String heading = rawHeading
                .replaceFirst("^\\d+(\\.\\d+)*\\s*", "")
                .replaceFirst("^[IVXLCDM]+\\.\\s*", "")
                .trim();

        if (heading.isEmpty()) {
            return rawHeading.trim();
        }

        return heading;
    }

    private static String extractSectionContent(List<PaperSection> sections, String sectionName) {
        return sections.stream()
                .filter(section -> section.getSectionName() != null)
                .filter(section -> section.getSectionName().trim().equalsIgnoreCase(sectionName))
                .map(PaperSection::getContent)
                .filter(content -> content != null && !content.isBlank())
                .findFirst()
                .orElse(sectionName.equalsIgnoreCase("keywords")
                        ? "research, paper"
                        : "No " + sectionName + " available.");
    }

    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    result.put(key, args[i + 1]);
                    i++;
                } else {
                    result.put(key, "true");
                }
            }
        }
        return result;
    }
}
