package com.draftly.repository;

import com.draftly.model.Paper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaperRepository extends MongoRepository<Paper, String> {
    List<Paper> findByDomain(String domain);
    List<Paper> findByKeywordsIn(List<String> keywords);
    List<Paper> findByPublicationYearBetween(int startYear, int endYear);
    Optional<Paper> findByDoi(String doi);
}
