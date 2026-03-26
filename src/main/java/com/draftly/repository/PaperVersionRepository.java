package com.draftly.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.draftly.model.PaperVersion;

@Repository
public interface PaperVersionRepository extends MongoRepository<PaperVersion, String> {
    List<PaperVersion> findBySubmissionIdOrderByVersionNumberAsc(String submissionId);
}
