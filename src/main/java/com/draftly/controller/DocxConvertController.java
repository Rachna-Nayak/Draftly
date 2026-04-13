package com.draftly.controller;

import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.ExportService;
import com.draftly.service.ResearchPaperService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/convert")
public class DocxConvertController {

    private final ResearchPaperService researchPaperService;
    private final ExportService exportService;

    public DocxConvertController(ResearchPaperService researchPaperService,
                                 ExportService exportService) {
        this.researchPaperService = researchPaperService;
        this.exportService = exportService;
    }

    /**
     * Convert an uploaded DOCX file to LaTeX and return the resulting .tex content.
     *
     * This does NOT persist anything long-term; it uses a temporary project/paper underneath
     * to reuse the existing DOCX import + LaTeX export pipeline.
     */
    @PostMapping(path = "/docx-to-latex", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<?> convertDocxToLatex(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Uploaded file is empty");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".docx")) {
            return ResponseEntity.badRequest().body("Only .docx files are supported");
        }

        byte[] docxBytes = file.getBytes();

        // Use a synthetic project and title; this is a transient conversion.
        String tempProjectId = "TEMP-CONVERT-" + UUID.randomUUID();
        String title = file.getOriginalFilename();

        // Import DOCX into a ResearchPaper (parsing into sections)
        var paper = researchPaperService.importFromDocx(tempProjectId, title, docxBytes, "IEEE");

        // Export that paper as LaTeX using the existing export pipeline
        String latex = exportService.exportAsLatex(paper.getId());

        // Some export implementations might contain Windows newlines; normalize if desired
        String normalizedLatex = latex.lines().collect(Collectors.joining("\n"));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of("latex", normalizedLatex));
    }
}
