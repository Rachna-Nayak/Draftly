package com.draftly.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.draftly.model.SubmissionFeedback;

@Repository
public interface SubmissionFeedbackRepository extends MongoRepository<SubmissionFeedback, String> {
    List<SubmissionFeedback> findBySubmissionId(String submissionId);
}
