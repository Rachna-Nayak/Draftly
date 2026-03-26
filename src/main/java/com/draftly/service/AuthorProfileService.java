package com.draftly.service;

import com.draftly.model.AuthorProfile;
import com.draftly.repository.AuthorProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for author profile management.
 */
@Service
public class AuthorProfileService {

    private final AuthorProfileRepository authorProfileRepository;

    public AuthorProfileService(AuthorProfileRepository authorProfileRepository) {
        this.authorProfileRepository = authorProfileRepository;
    }

    public AuthorProfile createProfile(AuthorProfile authorProfile) {
        return authorProfileRepository.save(authorProfile);
    }

    public Optional<AuthorProfile> getById(String id) {
        return authorProfileRepository.findById(id);
    }

    public Optional<AuthorProfile> getByUserId(String userId) {
        return authorProfileRepository.findByUserId(userId);
    }

    public List<AuthorProfile> getAllProfiles() {
        return authorProfileRepository.findAll();
    }

    public List<AuthorProfile> findByDomain(String domain) {
        return authorProfileRepository.findByDomainInterestsContaining(domain);
    }

    public AuthorProfile addProject(String userId, String projectId) {
        AuthorProfile profile = authorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Author profile not found for userId: " + userId));

        profile.addProjectId(projectId);
        return authorProfileRepository.save(profile);
    }

    public AuthorProfile addSubmission(String userId, String submissionId) {
        AuthorProfile profile = authorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Author profile not found for userId: " + userId));

        profile.addSubmissionId(submissionId);
        return authorProfileRepository.save(profile);
    }

    public void deleteProfile(String id) {
        authorProfileRepository.deleteById(id);
    }
}
