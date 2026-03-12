package com.draftly.controller;

import com.draftly.model.Paper;
import com.draftly.model.Reference;
import com.draftly.service.ReferenceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Reference & Citation management. (UC4)
 */
@Controller
@RequestMapping("/references")
public class ReferenceController {

    private final ReferenceService referenceService;

    public ReferenceController(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    @GetMapping("/{projectId}")
    public String listReferences(@PathVariable String projectId, Model model) {
        List<Reference> references = referenceService.getReferencesByProject(projectId);
        model.addAttribute("references", references);
        model.addAttribute("projectId", projectId);
        return "reference/list";
    }

    @PostMapping("/add")
    public String addReference(@RequestParam String projectId,
                               @RequestParam String paperId,
                               @RequestParam(defaultValue = "APA") String format) {
        referenceService.addReference(projectId, paperId, format);
        return "redirect:/references/" + projectId;
    }

    @PostMapping("/{referenceId}/delete")
    public String removeReference(@PathVariable String referenceId,
                                  @RequestParam String projectId) {
        referenceService.removeReference(referenceId);
        return "redirect:/references/" + projectId;
    }

    @GetMapping("/{projectId}/suggestions")
    public String suggestReferences(@PathVariable String projectId, Model model) {
        List<Paper> suggestions = referenceService.suggestReferences(projectId);
        model.addAttribute("suggestions", suggestions);
        model.addAttribute("projectId", projectId);
        return "reference/suggestions";
    }
}
