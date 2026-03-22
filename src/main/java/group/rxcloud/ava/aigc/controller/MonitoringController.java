package group.rxcloud.ava.aigc.controller;

import group.rxcloud.ava.aigc.dto.monitoring.MonitoringMetrics;
import group.rxcloud.ava.aigc.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Monitoring and Analytics REST Controller
 * Provides system metrics and health checks
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MonitoringController {

    private final MonitoringService monitoringService;

    /**
     * Get full monitoring metrics
     * Admin only endpoint
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MonitoringMetrics> getMetrics() {
        MonitoringMetrics metrics = monitoringService.getMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get health status
     * Public endpoint for health checks
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        MonitoringService.HealthStatus health = monitoringService.getHealthStatus();

        Map<String, Object> response = new HashMap<>();
        response.put("status", health.getStatus());
        response.put("timestamp", health.getTimestamp());
        response.put("cpuLoad", health.getCpuLoad());

        return ResponseEntity.ok(response);
    }

    /**
     * Get basic system info
     * Public endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getSystemInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "AVA AI Travel Guide");
        info.put("version", "1.0.0");
        info.put("status", "running");

        return ResponseEntity.ok(info);
    }

    /**
     * Reset metrics (admin only)
     */
    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetMetrics() {
        monitoringService.resetMetrics();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Metrics reset successfully");

        return ResponseEntity.ok(response);
    }
}
