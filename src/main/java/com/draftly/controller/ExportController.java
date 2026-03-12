package com.draftly.controller;

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
    public ExportService.ReadinessReport checkReadiness(@PathVariable String paperId,
                                                         @RequestParam String projectId) {
        return exportService.checkReadiness(paperId, projectId);
    }

    @GetMapping("/pdf/{paperId}")
    public ResponseEntity<byte[]> exportPDF(@PathVariable String paperId) {
        byte[] pdf = exportService.exportAsPDF(paperId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=paper.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/word/{paperId}")
    public ResponseEntity<byte[]> exportWord(@PathVariable String paperId) {
        byte[] word = exportService.exportAsWord(paperId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=paper.docx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(word);
    }

    @GetMapping("/latex/{paperId}")
    public ResponseEntity<String> exportLatex(@PathVariable String paperId) {
        String latex = exportService.exportAsLatex(paperId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=paper.tex")
                .contentType(MediaType.TEXT_PLAIN)
                .body(latex);
    }
}
