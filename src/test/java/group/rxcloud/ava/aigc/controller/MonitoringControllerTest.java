package group.rxcloud.ava.aigc.controller;

import group.rxcloud.ava.aigc.dto.monitoring.MonitoringMetrics;
import group.rxcloud.ava.aigc.service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MonitoringController using MockMvc
 */
@WebMvcTest(MonitoringController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable security filters for unit testing
public class MonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonitoringService monitoringService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetMetrics_AdminAccess() throws Exception {
        // Arrange
        MonitoringMetrics mockMetrics = createMockMetrics();
        when(monitoringService.getMetrics()).thenReturn(mockMetrics);

        // Act & Assert
        mockMvc.perform(get("/api/monitoring/metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.system").exists())
                .andExpect(jsonPath("$.application").exists())
                .andExpect(jsonPath("$.api").exists())
                .andExpect(jsonPath("$.user").exists());

        verify(monitoringService, times(1)).getMetrics();
    }

    @Test
    void testGetMetrics_PublicAccess() throws Exception {
        // This endpoint requires ADMIN role, so it should fail without auth
        mockMvc.perform(get("/api/monitoring/metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetHealth_PublicAccess() throws Exception {
        // Arrange
        MonitoringService.HealthStatus mockHealth = MonitoringService.HealthStatus.builder()
                .status("UP")
                .cpuLoad(0.5)
                .timestamp(LocalDateTime.now())
                .build();
        when(monitoringService.getHealthStatus()).thenReturn(mockHealth);

        // Act & Assert - health endpoint is public
        mockMvc.perform(get("/api/monitoring/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.cpuLoad").value(0.5))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(monitoringService, times(1)).getHealthStatus();
    }

    @Test
    void testGetSystemInfo_PublicAccess() throws Exception {
        // Act & Assert - info endpoint is public
        mockMvc.perform(get("/api/monitoring/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("AVA AI Travel Guide"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.status").value("running"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testResetMetrics_AdminAccess() throws Exception {
        // Arrange
        doNothing().when(monitoringService).resetMetrics();

        // Act & Assert
        mockMvc.perform(post("/api/monitoring/reset")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Metrics reset successfully"));

        verify(monitoringService, times(1)).resetMetrics();
    }

    @Test
    void testResetMetrics_PublicAccess() throws Exception {
        // This endpoint requires ADMIN role, so it should fail without auth
        mockMvc.perform(post("/api/monitoring/reset")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetHealth_WarningStatus() throws Exception {
        // Arrange
        MonitoringService.HealthStatus mockHealth = MonitoringService.HealthStatus.builder()
                .status("WARNING")
                .cpuLoad(0.9)
                .timestamp(LocalDateTime.now())
                .build();
        when(monitoringService.getHealthStatus()).thenReturn(mockHealth);

        // Act & Assert
        mockMvc.perform(get("/api/monitoring/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WARNING"))
                .andExpect(jsonPath("$.cpuLoad").value(0.9));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetMetrics_UserRoleAccess() throws Exception {
        // USER role should not have access to metrics endpoint
        mockMvc.perform(get("/api/monitoring/metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // Helper method to create mock metrics
    private MonitoringMetrics createMockMetrics() {
        return MonitoringMetrics.builder()
                .timestamp(LocalDateTime.now())
                .system(MonitoringMetrics.SystemMetrics.builder()
                        .cpuUsage(50.0)
                        .memoryUsed(512)
                        .memoryTotal(1024)
                        .memoryUsagePercentage(50.0)
                        .diskUsed(1000)
                        .diskTotal(2000)
                        .diskUsagePercentage(50.0)
                        .uptime(3600)
                        .build())
                .application(MonitoringMetrics.ApplicationMetrics.builder()
                        .activeConnections(10)
                        .threadCount(20)
                        .peakThreadCount(25)
                        .totalRequests(1000)
                        .errorCount(10)
                        .errorRate(1.0)
                        .build())
                .api(MonitoringMetrics.ApiMetrics.builder()
                        .totalRequests(1000)
                        .successfulRequests(990)
                        .failedRequests(10)
                        .averageResponseTime(150.0)
                        .endpoints(new HashMap<>())
                        .build())
                .user(MonitoringMetrics.UserMetrics.builder()
                        .totalUsers(100)
                        .activeUsers(50)
                        .newUsersToday(5)
                        .totalConversations(500)
                        .totalMessages(5000)
                        .build())
                .build();
    }
}
