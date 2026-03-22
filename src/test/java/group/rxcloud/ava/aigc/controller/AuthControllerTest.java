package group.rxcloud.ava.aigc.controller;

import group.rxcloud.ava.aigc.dto.auth.AuthResponse;
import group.rxcloud.ava.aigc.dto.auth.LoginRequest;
import group.rxcloud.ava.aigc.dto.auth.RefreshTokenRequest;
import group.rxcloud.ava.aigc.dto.auth.RegisterRequest;
import group.rxcloud.ava.aigc.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController using MockMvc
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable security filters for unit testing
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setDisplayName("Test User");

        AuthResponse response = AuthResponse.builder()
                .success(true)
                .message("User registered successfully")
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testRegister_Failure() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .success(false)
                .message("Username already exists")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_Failure() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        AuthResponse response = AuthResponse.builder()
                .success(false)
                .message("Invalid credentials")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        AuthResponse response = AuthResponse.builder()
                .success(true)
                .message("Token refreshed successfully")
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));

        verify(authService, times(1)).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void testRefreshToken_Failure() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-refresh-token");

        AuthResponse response = AuthResponse.builder()
                .success(false)
                .message("Invalid refresh token")
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void testHealth() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Auth service is running"));
    }

    @Test
    void testRegister_InvalidRequest() throws Exception {
        // Arrange - missing required fields
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");  // Empty username
        request.setEmail("invalid-email");  // Invalid email
        request.setPassword("123");  // Too short password

        // Act & Assert - should fail validation
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_InvalidRequest() throws Exception {
        // Arrange - missing required fields
        LoginRequest request = new LoginRequest();
        request.setUsername("");  // Empty username
        request.setPassword("");  // Empty password

        // Act & Assert - should fail validation
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
