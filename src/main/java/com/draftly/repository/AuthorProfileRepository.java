package com.draftly.repository;

import com.draftly.model.AuthorProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorProfileRepository extends MongoRepository<AuthorProfile, String> {
    Optional<AuthorProfile> findByUserId(String userId);
    List<AuthorProfile> findByDomainInterestsContaining(String domain);
}
