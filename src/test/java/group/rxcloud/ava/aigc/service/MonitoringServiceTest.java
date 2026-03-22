package group.rxcloud.ava.aigc.service;

import group.rxcloud.ava.aigc.dto.monitoring.MonitoringMetrics;
import group.rxcloud.ava.aigc.repository.ConversationRepository;
import group.rxcloud.ava.aigc.repository.MessageRepository;
import group.rxcloud.ava.aigc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MonitoringService
 */
@ExtendWith(MockitoExtension.class)
public class MonitoringServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MonitoringService monitoringService;

    @BeforeEach
    void setUp() {
        monitoringService.resetMetrics();
    }

    @Test
    void testRecordRequest_Success() {
        // Act
        monitoringService.recordRequest("/api/test", "GET", 100, true);

        // Assert - verify metrics were recorded by checking API metrics
        MonitoringMetrics metrics = monitoringService.getMetrics();
        assertNotNull(metrics);
        assertNotNull(metrics.getApi());
        assertEquals(1, metrics.getApi().getTotalRequests());
        assertEquals(1, metrics.getApi().getSuccessfulRequests());
        assertEquals(0, metrics.getApi().getFailedRequests());
    }

    @Test
    void testRecordRequest_Failure() {
        // Act
        monitoringService.recordRequest("/api/test", "GET", 100, false);

        // Assert
        MonitoringMetrics metrics = monitoringService.getMetrics();
        assertNotNull(metrics);
        assertEquals(1, metrics.getApi().getTotalRequests());
        assertEquals(0, metrics.getApi().getSuccessfulRequests());
        assertEquals(1, metrics.getApi().getFailedRequests());
    }

    @Test
    void testRecordRequest_MultipleRequests() {
        // Act
        monitoringService.recordRequest("/api/test1", "GET", 100, true);
        monitoringService.recordRequest("/api/test2", "POST", 200, true);
        monitoringService.recordRequest("/api/test3", "GET", 50, false);

        // Assert
        MonitoringMetrics metrics = monitoringService.getMetrics();
        assertNotNull(metrics);
        assertEquals(3, metrics.getApi().getTotalRequests());
        assertEquals(2, metrics.getApi().getSuccessfulRequests());
        assertEquals(1, metrics.getApi().getFailedRequests());
    }

    @Test
    void testGetMetrics() {
        // Arrange
        when(userRepository.count()).thenReturn(10L);
        when(conversationRepository.count()).thenReturn(20L);
        when(messageRepository.count()).thenReturn(100L);
        when(userRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(2L);

        // Act
        MonitoringMetrics metrics = monitoringService.getMetrics();

        // Assert
        assertNotNull(metrics);
        assertNotNull(metrics.getTimestamp());
        assertNotNull(metrics.getSystem());
        assertNotNull(metrics.getApplication());
        assertNotNull(metrics.getApi());
        assertNotNull(metrics.getUser());

        // Verify user metrics
        assertEquals(10L, metrics.getUser().getTotalUsers());
        assertEquals(20L, metrics.getUser().getTotalConversations());
        assertEquals(100L, metrics.getUser().getTotalMessages());
        assertEquals(2L, metrics.getUser().getNewUsersToday());

        // Verify system metrics
        assertTrue(metrics.getSystem().getUptime() >= 0);
        assertTrue(metrics.getSystem().getMemoryTotal() > 0);
        assertTrue(metrics.getSystem().getMemoryUsed() > 0);

        // Verify application metrics
        assertTrue(metrics.getApplication().getThreadCount() > 0);
    }

    @Test
    void testGetHealthStatus_Healthy() {
        // Act
        MonitoringService.HealthStatus health = monitoringService.getHealthStatus();

        // Assert
        assertNotNull(health);
        assertNotNull(health.getStatus());
        assertTrue(health.getStatus().equals("UP") || health.getStatus().equals("WARNING"));
        assertNotNull(health.getTimestamp());
    }

    @Test
    void testResetMetrics() {
        // Arrange - record some requests
        monitoringService.recordRequest("/api/test", "GET", 100, true);
        monitoringService.recordRequest("/api/test", "GET", 200, false);

        // Verify metrics exist
        MonitoringMetrics metricsBefore = monitoringService.getMetrics();
        assertEquals(2, metricsBefore.getApi().getTotalRequests());

        // Act
        monitoringService.resetMetrics();

        // Assert
        MonitoringMetrics metricsAfter = monitoringService.getMetrics();
        assertEquals(0, metricsAfter.getApi().getTotalRequests());
        assertEquals(0, metricsAfter.getApi().getSuccessfulRequests());
        assertEquals(0, metricsAfter.getApi().getFailedRequests());
    }

    @Test
    void testEndpointStats_AverageResponseTime() {
        // Act
        monitoringService.recordRequest("/api/test", "GET", 100, true);
        monitoringService.recordRequest("/api/test", "GET", 200, true);
        monitoringService.recordRequest("/api/test", "GET", 300, true);

        // Assert
        MonitoringMetrics metrics = monitoringService.getMetrics();
        assertNotNull(metrics.getApi().getEndpoints());

        MonitoringMetrics.EndpointMetrics endpointMetrics =
                metrics.getApi().getEndpoints().get("GET /api/test");
        assertNotNull(endpointMetrics);
        assertEquals(3, endpointMetrics.getRequestCount());
        assertEquals(200.0, endpointMetrics.getAverageResponseTime(), 0.01);
        assertEquals(100.0, endpointMetrics.getMinResponseTime(), 0.01);
        assertEquals(300.0, endpointMetrics.getMaxResponseTime(), 0.01);
    }

    @Test
    void testEndpointStats_ErrorCount() {
        // Act
        monitoringService.recordRequest("/api/test", "GET", 100, true);
        monitoringService.recordRequest("/api/test", "GET", 100, false);
        monitoringService.recordRequest("/api/test", "GET", 100, false);

        // Assert
        MonitoringMetrics metrics = monitoringService.getMetrics();
        MonitoringMetrics.EndpointMetrics endpointMetrics =
                metrics.getApi().getEndpoints().get("GET /api/test");

        assertNotNull(endpointMetrics);
        assertEquals(3, endpointMetrics.getRequestCount());
        assertEquals(1, endpointMetrics.getSuccessCount());
        assertEquals(2, endpointMetrics.getErrorCount());
    }

    @Test
    void testCalculateAverageResponseTime() {
        // Act - record requests with different response times
        monitoringService.recordRequest("/api/fast", "GET", 50, true);
        monitoringService.recordRequest("/api/slow", "GET", 500, true);

        // Assert
        MonitoringMetrics metrics = monitoringService.getMetrics();
        double averageResponseTime = metrics.getApi().getAverageResponseTime();

        // Average of 50ms and 500ms
        assertEquals(275.0, averageResponseTime, 0.01);
    }

    @Test
    void testCalculateAverageResponseTime_Empty() {
        // Assert - no requests recorded
        MonitoringMetrics metrics = monitoringService.getMetrics();
        double averageResponseTime = metrics.getApi().getAverageResponseTime();

        assertEquals(0.0, averageResponseTime, 0.01);
    }

    @Test
    void testErrorRateCalculation() {
        // Arrange
        monitoringService.recordRequest("/api/test", "GET", 100, true);
        monitoringService.recordRequest("/api/test", "GET", 100, true);
        monitoringService.recordRequest("/api/test", "GET", 100, false);
        monitoringService.recordRequest("/api/test", "GET", 100, false);
        monitoringService.recordRequest("/api/test", "GET", 100, false);

        // Act
        MonitoringMetrics metrics = monitoringService.getMetrics();

        // Assert - 3 errors out of 5 requests = 60% error rate
        assertEquals(60.0, metrics.getApplication().getErrorRate(), 0.01);
    }
}
