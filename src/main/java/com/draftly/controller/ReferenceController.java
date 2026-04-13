package com.draftly.controller;

import com.draftly.model.Paper;
import com.draftly.model.Reference;
import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.ReferenceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Reference & Citation management. (UC4)
 */
@RestController
@RequestMapping("/api/references")
public class ReferenceController {

    private final ReferenceService referenceService;

    public ReferenceController(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    @GetMapping("/{projectId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<Reference> listReferences(@PathVariable String projectId) {
        return referenceService.getReferencesByProject(projectId);
    }

    @PostMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public Reference addReference(@RequestBody Map<String, String> body) {
        return referenceService.addReference(
            body.get("projectId"),
            body.get("paperId"),
            body.getOrDefault("format", "APA")
        );
    }

    @DeleteMapping("/{referenceId}")
    @RequireRoles({UserRole.AUTHOR, UserRole.ADMIN})
    public ResponseEntity<Void> removeReference(@PathVariable String referenceId) {
        referenceService.removeReference(referenceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/suggestions")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<Paper> suggestReferences(@PathVariable String projectId) {
        return referenceService.suggestReferences(projectId);
    }

    @GetMapping("/{projectId}/export/bibtex")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<String> exportBibtex(@PathVariable String projectId) {
        String bibtex = referenceService.exportAsBibtex(projectId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=references.bib")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bibtex);
    }

    @GetMapping("/{projectId}/export/plaintext")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<String> exportPlaintext(@PathVariable String projectId,
                                                   @RequestParam(defaultValue = "APA") String format) {
        String plaintext = referenceService.exportAsPlaintext(projectId, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=references.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(plaintext);
    }

    @GetMapping("/{projectId}/export/html")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<String> exportHtml(@PathVariable String projectId,
                                              @RequestParam(defaultValue = "APA") String format) {
        String html = referenceService.exportAsHtml(projectId, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=references.html")
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
