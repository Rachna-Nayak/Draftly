package com.draftly.repository;

import com.draftly.model.ReviewSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewScheduleRepository extends MongoRepository<ReviewSchedule, String> {
    List<ReviewSchedule> findByConferenceId(String conferenceId);

    List<ReviewSchedule> findBySubmissionId(String submissionId);

    List<ReviewSchedule> findByReviewerUserId(String reviewerUserId);
}