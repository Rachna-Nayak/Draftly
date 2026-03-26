package com.draftly.repository;

import com.draftly.model.Conference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConferenceRepository extends MongoRepository<Conference, String> {
    Optional<Conference> findByAcronym(String acronym);
    List<Conference> findByDomain(String domain);
    List<Conference> findByTrackNamesContaining(String trackName);
}
