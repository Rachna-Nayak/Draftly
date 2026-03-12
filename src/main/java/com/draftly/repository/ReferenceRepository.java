package com.draftly.repository;

import com.draftly.model.Reference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferenceRepository extends MongoRepository<Reference, String> {
    List<Reference> findByProjectId(String projectId);
    List<Reference> findByPaperId(String paperId);
}
