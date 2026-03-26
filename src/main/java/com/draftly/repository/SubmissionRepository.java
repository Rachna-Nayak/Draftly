package com.draftly.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.draftly.model.Submission;

@Repository
public interface SubmissionRepository extends MongoRepository<Submission, String> {
    List<Submission> findByAuthorId(String authorId);
    List<Submission> findByProjectId(String projectId);
    List<Submission> findByStatus(Submission.Status status);
}
