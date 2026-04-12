package com.draftly.controller;

import com.draftly.model.MetricsDashboard;
import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
import com.draftly.service.MetricsDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for dashboard metrics operations.
 */
@RestController
@RequestMapping("/api/metrics-dashboards")
public class MetricsDashboardController {

    private final MetricsDashboardService metricsDashboardService;

    public MetricsDashboardController(MetricsDashboardService metricsDashboardService) {
        this.metricsDashboardService = metricsDashboardService;
    }

    @PostMapping
    @RequireRoles({UserRole.ADMIN})
    public MetricsDashboard createDashboard(@RequestBody MetricsDashboard dashboard) {
        return metricsDashboardService.createDashboard(dashboard);
    }

    @GetMapping
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public List<MetricsDashboard> listDashboards() {
        return metricsDashboardService.getAllDashboards();
    }

    @GetMapping("/{id}")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<MetricsDashboard> getById(@PathVariable String id) {
        return metricsDashboardService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/scope")
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public ResponseEntity<MetricsDashboard> getByScope(@RequestParam String scopeType,
                                                       @RequestParam String scopeId) {
        return metricsDashboardService.getByScope(scopeType, scopeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @RequireRoles({UserRole.ADMIN})
    public MetricsDashboard updateDashboard(@PathVariable String id,
                                            @RequestBody MetricsDashboard dashboard) {
        dashboard.setId(id);
        return metricsDashboardService.updateDashboard(dashboard);
    }

    @DeleteMapping("/{id}")
    @RequireRoles({UserRole.ADMIN})
    public ResponseEntity<Void> deleteDashboard(@PathVariable String id) {
        metricsDashboardService.deleteDashboard(id);
        return ResponseEntity.noContent().build();
    }
}
