package com.draftly.service;

import com.draftly.model.Conference;
import com.draftly.repository.ConferenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for conference management.
 */
@Service
public class ConferenceService {

    private final ConferenceRepository conferenceRepository;

    public ConferenceService(ConferenceRepository conferenceRepository) {
        this.conferenceRepository = conferenceRepository;
    }

    public Conference createConference(Conference conference) {
        return conferenceRepository.save(conference);
    }

    public List<Conference> getAllConferences() {
        return conferenceRepository.findAll();
    }

    public Optional<Conference> getConferenceById(String id) {
        return conferenceRepository.findById(id);
    }

    public Optional<Conference> getConferenceByAcronym(String acronym) {
        return conferenceRepository.findByAcronym(acronym);
    }

    public List<Conference> getConferencesByDomain(String domain) {
        return conferenceRepository.findByDomain(domain);
    }

    public List<Conference> getConferencesByTrack(String trackName) {
        return conferenceRepository.findByTrackNamesContaining(trackName);
    }

    public Conference updateConference(Conference conference) {
        return conferenceRepository.save(conference);
    }

    public void deleteConference(String id) {
        conferenceRepository.deleteById(id);
    }
}
