package com.draftly.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.draftly.model.PublishedPaper;

@Repository
public interface PublishedPaperRepository extends MongoRepository<PublishedPaper, String> {
    Optional<PublishedPaper> findBySubmissionId(String submissionId);
}
