package com.draftly.controller;

import com.draftly.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Export & Submission Readiness Check. (UC8)
 */
@Controller
@RequestMapping("/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/readiness/{paperId}")
    public String checkReadiness(@PathVariable String paperId,
                                 @RequestParam String projectId,
                                 Model model) {
        ExportService.ReadinessReport report = exportService.checkReadiness(paperId, projectId);
        model.addAttribute("report", report);
        model.addAttribute("paperId", paperId);
        model.addAttribute("projectId", projectId);
        return "export/readiness";
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
