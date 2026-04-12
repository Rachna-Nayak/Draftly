package com.draftly.controller;

import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Export & Submission Readiness Check. (UC8)
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/readiness/{paperId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ExportService.ReadinessReport checkReadiness(@PathVariable String paperId,
                                                         @RequestParam String projectId) {
        return exportService.checkReadiness(paperId, projectId);
    }

    @GetMapping("/pdf/{paperId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<byte[]> exportPDF(@PathVariable String paperId) {
        byte[] pdf = exportService.exportAsPDF(paperId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=paper.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/word/{paperId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<byte[]> exportWord(@PathVariable String paperId) {
        byte[] word = exportService.exportAsWord(paperId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=paper.docx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(word);
    }

    @GetMapping("/latex/{paperId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<String> exportLatex(@PathVariable String paperId) {
        String latex = exportService.exportAsLatex(paperId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=paper.tex")
                .contentType(MediaType.TEXT_PLAIN)
                .body(latex);
    }

    @GetMapping("/bib/{paperId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<String> exportBib(@PathVariable String paperId) {
        String bib = exportService.exportBibForPaper(paperId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=references.bib")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bib);
    }
}
