package group.rxcloud.ava.aigc.service;

import group.rxcloud.ava.aigc.dto.monitoring.MonitoringMetrics;
import group.rxcloud.ava.aigc.entity.User;
import group.rxcloud.ava.aigc.repository.ConversationRepository;
import group.rxcloud.ava.aigc.repository.MessageRepository;
import group.rxcloud.ava.aigc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for collecting and managing system monitoring metrics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    // In-memory metrics storage
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final Map<String, EndpointStats> endpointStats = new ConcurrentHashMap<>();

    /**
     * Record API request
     */
    public void recordRequest(String endpoint, String method, long responseTime, boolean success) {
        totalRequests.incrementAndGet();
        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }

        String key = method + " " + endpoint;
        EndpointStats stats = endpointStats.computeIfAbsent(key, k -> new EndpointStats());
        stats.recordRequest(responseTime, success);
    }

    /**
     * Get current monitoring metrics
     */
    public MonitoringMetrics getMetrics() {
        return MonitoringMetrics.builder()
                .timestamp(LocalDateTime.now())
                .system(collectSystemMetrics())
                .application(collectApplicationMetrics())
                .api(collectApiMetrics())
                .user(collectUserMetrics())
                .build();
    }

    /**
     * Get health status
     */
    public HealthStatus getHealthStatus() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemLoadAverage();

        // Check if CPU load is acceptable (less than 80%)
        boolean isHealthy = cpuLoad < 0.8;

        return HealthStatus.builder()
                .status(isHealthy ? "UP" : "WARNING")
                .cpuLoad(cpuLoad)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Collect system metrics
     */
    private MonitoringMetrics.SystemMetrics collectSystemMetrics() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return MonitoringMetrics.SystemMetrics.builder()
                .cpuUsage(osBean.getSystemLoadAverage() * 100)
                .memoryUsed(usedMemory / 1024 / 1024) // MB
                .memoryTotal(totalMemory / 1024 / 1024) // MB
                .memoryUsagePercentage((double) usedMemory / totalMemory * 100)
                .diskUsed(0) // Would need additional implementation
                .diskTotal(0)
                .diskUsagePercentage(0)
                .uptime(runtimeBean.getUptime() / 1000) // seconds
                .build();
    }

    /**
     * Collect application metrics
     */
    private MonitoringMetrics.ApplicationMetrics collectApplicationMetrics() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long total = totalRequests.get();
        long errors = failedRequests.get();
        double errorRate = total > 0 ? (double) errors / total * 100 : 0;

        return MonitoringMetrics.ApplicationMetrics.builder()
                .activeConnections(0) // Would need additional implementation
                .threadCount(threadBean.getThreadCount())
                .peakThreadCount(threadBean.getPeakThreadCount())
                .totalRequests(total)
                .errorCount(errors)
                .errorRate(errorRate)
                .build();
    }

    /**
     * Collect API metrics
     */
    private MonitoringMetrics.ApiMetrics collectApiMetrics() {
        Map<String, MonitoringMetrics.EndpointMetrics> endpointMetrics = new ConcurrentHashMap<>();

        endpointStats.forEach((key, stats) -> {
            String[] parts = key.split(" ", 2);
            endpointMetrics.put(key, MonitoringMetrics.EndpointMetrics.builder()
                    .path(parts.length > 1 ? parts[1] : key)
                    .method(parts.length > 0 ? parts[0] : "UNKNOWN")
                    .requestCount(stats.getRequestCount())
                    .successCount(stats.getSuccessCount())
                    .errorCount(stats.getErrorCount())
                    .averageResponseTime(stats.getAverageResponseTime())
                    .minResponseTime(stats.getMinResponseTime())
                    .maxResponseTime(stats.getMaxResponseTime())
                    .build());
        });

        long total = totalRequests.get();
        long success = successfulRequests.get();

        return MonitoringMetrics.ApiMetrics.builder()
                .totalRequests(total)
                .successfulRequests(success)
                .failedRequests(failedRequests.get())
                .averageResponseTime(calculateAverageResponseTime())
                .endpoints(endpointMetrics)
                .build();
    }

    /**
     * Collect user metrics
     */
    private MonitoringMetrics.UserMetrics collectUserMetrics() {
        long totalUsers = userRepository.count();
        long totalConversations = conversationRepository.count();
        long totalMessages = messageRepository.count();

        // Count new users today
        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        long newUsersToday = userRepository.countByCreatedAtAfter(startOfDay);

        return MonitoringMetrics.UserMetrics.builder()
                .totalUsers(totalUsers)
                .activeUsers(totalUsers) // Would need session tracking
                .newUsersToday(newUsersToday)
                .totalConversations(totalConversations)
                .totalMessages(totalMessages)
                .build();
    }

    /**
     * Calculate average response time across all endpoints
     */
    private double calculateAverageResponseTime() {
        if (endpointStats.isEmpty()) {
            return 0;
        }
        return endpointStats.values().stream()
                .mapToDouble(EndpointStats::getAverageResponseTime)
                .average()
                .orElse(0);
    }

    /**
     * Reset metrics (useful for testing)
     */
    public void resetMetrics() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        endpointStats.clear();
    }

    /**
     * Inner class for tracking endpoint statistics
     */
    private static class EndpointStats {
        private final AtomicLong requestCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private volatile double minResponseTime = Double.MAX_VALUE;
        private volatile double maxResponseTime = 0;

        public void recordRequest(long responseTime, boolean success) {
            requestCount.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
            if (success) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }

            // Update min/max
            synchronized (this) {
                if (responseTime < minResponseTime) {
                    minResponseTime = responseTime;
                }
                if (responseTime > maxResponseTime) {
                    maxResponseTime = responseTime;
                }
            }
        }

        public long getRequestCount() {
            return requestCount.get();
        }

        public long getSuccessCount() {
            return successCount.get();
        }

        public long getErrorCount() {
            return errorCount.get();
        }

        public double getAverageResponseTime() {
            long count = requestCount.get();
            return count > 0 ? (double) totalResponseTime.get() / count : 0;
        }

        public double getMinResponseTime() {
            return minResponseTime == Double.MAX_VALUE ? 0 : minResponseTime;
        }

        public double getMaxResponseTime() {
            return maxResponseTime;
        }
    }

    /**
     * Health status DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class HealthStatus {
        private String status;
        private double cpuLoad;
        private LocalDateTime timestamp;
    }
}
