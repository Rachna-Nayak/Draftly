package com.draftly.controller;

import com.draftly.model.Conference;
import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.ConferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for conference management.
 */
@RestController
@RequestMapping("/api/conferences")
public class ConferenceController {

    private final ConferenceService conferenceService;

    public ConferenceController(ConferenceService conferenceService) {
        this.conferenceService = conferenceService;
    }

    @PostMapping
    @RequireRoles({UserRole.ADMIN})
    public Conference createConference(@RequestBody Conference conference) {
        return conferenceService.createConference(conference);
    }

    @GetMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<Conference> listConferences() {
        return conferenceService.getAllConferences();
    }

    @GetMapping("/{id}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<Conference> getConferenceById(@PathVariable String id) {
        return conferenceService.getConferenceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/acronym/{acronym}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<Conference> getConferenceByAcronym(@PathVariable String acronym) {
        return conferenceService.getConferenceByAcronym(acronym)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/domain/{domain}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<Conference> getConferencesByDomain(@PathVariable String domain) {
        return conferenceService.getConferencesByDomain(domain);
    }

    @GetMapping("/track/{trackName}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<Conference> getConferencesByTrack(@PathVariable String trackName) {
        return conferenceService.getConferencesByTrack(trackName);
    }

    @PutMapping("/{id}")
    @RequireRoles({UserRole.ADMIN})
    public Conference updateConference(@PathVariable String id, @RequestBody Conference conference) {
        conference.setId(id);
        return conferenceService.updateConference(conference);
    }

    @DeleteMapping("/{id}")
    @RequireRoles({UserRole.ADMIN})
    public ResponseEntity<Void> deleteConference(@PathVariable String id) {
        conferenceService.deleteConference(id);
        return ResponseEntity.noContent().build();
    }
}
