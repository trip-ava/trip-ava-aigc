package group.rxcloud.ava.aigc.dto.monitoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * System monitoring metrics DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringMetrics {

    // Timestamp
    private LocalDateTime timestamp;

    // System metrics
    private SystemMetrics system;

    // Application metrics
    private ApplicationMetrics application;

    // API metrics
    private ApiMetrics api;

    // User metrics
    private UserMetrics user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemMetrics {
        private double cpuUsage;
        private long memoryUsed;
        private long memoryTotal;
        private double memoryUsagePercentage;
        private long diskUsed;
        private long diskTotal;
        private double diskUsagePercentage;
        private long uptime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationMetrics {
        private int activeConnections;
        private int threadCount;
        private int peakThreadCount;
        private long totalRequests;
        private long errorCount;
        private double errorRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiMetrics {
        private long totalRequests;
        private long successfulRequests;
        private long failedRequests;
        private double averageResponseTime;
        private Map<String, EndpointMetrics> endpoints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointMetrics {
        private String path;
        private String method;
        private long requestCount;
        private long successCount;
        private long errorCount;
        private double averageResponseTime;
        private double minResponseTime;
        private double maxResponseTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserMetrics {
        private long totalUsers;
        private long activeUsers;
        private long newUsersToday;
        private long totalConversations;
        private long totalMessages;
    }
}
