package com.draftly.repository;

import com.draftly.model.ResearchPaper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResearchPaperRepository extends MongoRepository<ResearchPaper, String> {
    List<ResearchPaper> findByProjectId(String projectId);
    List<ResearchPaper> findByStatus(String status);
}
