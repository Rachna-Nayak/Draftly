package com.draftly.service;

import com.draftly.model.MetricsDashboard;
import com.draftly.repository.MetricsDashboardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for dashboard metrics management.
 */
@Service
public class MetricsDashboardService {

    private final MetricsDashboardRepository metricsDashboardRepository;

    public MetricsDashboardService(MetricsDashboardRepository metricsDashboardRepository) {
        this.metricsDashboardRepository = metricsDashboardRepository;
    }

    public MetricsDashboard createDashboard(MetricsDashboard dashboard) {
        dashboard.setLastUpdatedAt(LocalDateTime.now());
        return metricsDashboardRepository.save(dashboard);
    }

    public Optional<MetricsDashboard> getById(String id) {
        return metricsDashboardRepository.findById(id);
    }

    public Optional<MetricsDashboard> getByScope(String scopeType, String scopeId) {
        return metricsDashboardRepository.findByScopeTypeAndScopeId(scopeType, scopeId);
    }

    public List<MetricsDashboard> getAllDashboards() {
        return metricsDashboardRepository.findAll();
    }

    public MetricsDashboard updateDashboard(MetricsDashboard dashboard) {
        dashboard.setLastUpdatedAt(LocalDateTime.now());
        return metricsDashboardRepository.save(dashboard);
    }

    public void deleteDashboard(String id) {
        metricsDashboardRepository.deleteById(id);
    }
}
