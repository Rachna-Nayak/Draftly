package com.draftly.repository;

import com.draftly.model.MetricsDashboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetricsDashboardRepository extends MongoRepository<MetricsDashboard, String> {
    Optional<MetricsDashboard> findByScopeTypeAndScopeId(String scopeType, String scopeId);
}
