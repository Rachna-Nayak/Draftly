package com.draftly.repository;

import com.draftly.model.ResearchProject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends MongoRepository<ResearchProject, String> {
    List<ResearchProject> findByOwnerId(String ownerId);
    List<ResearchProject> findByDomain(String domain);
}
