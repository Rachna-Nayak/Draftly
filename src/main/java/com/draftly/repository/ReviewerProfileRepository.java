package com.draftly.repository;

import com.draftly.model.ReviewerProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewerProfileRepository extends MongoRepository<ReviewerProfile, String> {
    Optional<ReviewerProfile> findByUserId(String userId);
    List<ReviewerProfile> findByExpertiseDomainsContaining(String domain);
}
