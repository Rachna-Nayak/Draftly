package com.draftly.controller;

import com.draftly.model.Conference;
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
    public Conference createConference(@RequestBody Conference conference) {
        return conferenceService.createConference(conference);
    }

    @GetMapping
    public List<Conference> listConferences() {
        return conferenceService.getAllConferences();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conference> getConferenceById(@PathVariable String id) {
        return conferenceService.getConferenceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/acronym/{acronym}")
    public ResponseEntity<Conference> getConferenceByAcronym(@PathVariable String acronym) {
        return conferenceService.getConferenceByAcronym(acronym)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/domain/{domain}")
    public List<Conference> getConferencesByDomain(@PathVariable String domain) {
        return conferenceService.getConferencesByDomain(domain);
    }

    @GetMapping("/track/{trackName}")
    public List<Conference> getConferencesByTrack(@PathVariable String trackName) {
        return conferenceService.getConferencesByTrack(trackName);
    }

    @PutMapping("/{id}")
    public Conference updateConference(@PathVariable String id, @RequestBody Conference conference) {
        conference.setId(id);
        return conferenceService.updateConference(conference);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConference(@PathVariable String id) {
        conferenceService.deleteConference(id);
        return ResponseEntity.noContent().build();
    }
}
