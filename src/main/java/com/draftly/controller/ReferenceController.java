package com.draftly.controller;

import com.draftly.model.Paper;
import com.draftly.model.Reference;
import com.draftly.service.ReferenceService;
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
    public List<Reference> listReferences(@PathVariable String projectId) {
        return referenceService.getReferencesByProject(projectId);
    }

    @PostMapping
    public Reference addReference(@RequestBody Map<String, String> body) {
        return referenceService.addReference(
            body.get("projectId"),
            body.get("paperId"),
            body.getOrDefault("format", "APA")
        );
    }

    @DeleteMapping("/{referenceId}")
    public ResponseEntity<Void> removeReference(@PathVariable String referenceId) {
        referenceService.removeReference(referenceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/suggestions")
    public List<Paper> suggestReferences(@PathVariable String projectId) {
        return referenceService.suggestReferences(projectId);
    }
}
