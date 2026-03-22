package group.rxcloud.ava.aigc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AvaController using MockMvc
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AvaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/ava/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void testHealthSimpleEndpoint() throws Exception {
        mockMvc.perform(get("/ava/health/simple"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void testHelloContentEndpoint() throws Exception {
        // Arrange
        AvaController.AvaHelloRequest request = new AvaController.AvaHelloRequest();
        request.setUserId("test-user");

        // Act & Assert
        mockMvc.perform(post("/ava/hello")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void testChatEndpoint_WithValidRequest() throws Exception {
        // Arrange
        AvaController.ChatRequest request = new AvaController.ChatRequest();
        request.setUserId("test-user");
        request.setConversationId("test-conv");

        AvaController.ChatMessage message = new AvaController.ChatMessage();
        message.setRole("user");
        message.setContent("你好");
        message.setTimestamp(System.currentTimeMillis());
        request.setMessage(message);

        // Act & Assert
        mockMvc.perform(post("/ava/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("test-conv"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testChatEndpoint_WithEmptyMessage() throws Exception {
        // Arrange
        AvaController.ChatRequest request = new AvaController.ChatRequest();
        request.setUserId("test-user");
        request.setConversationId("test-conv");

        AvaController.ChatMessage message = new AvaController.ChatMessage();
        message.setRole("user");
        message.setContent("");
        message.setTimestamp(System.currentTimeMillis());
        request.setMessage(message);

        // Act & Assert
        mockMvc.perform(post("/ava/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHelloVoiceEndpoint_WithValidContent() throws Exception {
        // Arrange
        AvaController.AvaHelloVoiceRequest request = new AvaController.AvaHelloVoiceRequest();
        request.setUserId("test-user");
        request.setContent("你好 AVA");

        // Act & Assert
        mockMvc.perform(post("/ava/hello/voice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testHelloVoiceEndpoint_WithEmptyContent() throws Exception {
        // Arrange
        AvaController.AvaHelloVoiceRequest request = new AvaController.AvaHelloVoiceRequest();
        request.setUserId("test-user");
        request.setContent("  ");

        // Act & Assert
        mockMvc.perform(post("/ava/hello/voice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVoiceChatEndpoint_WithValidAudio() throws Exception {
        // Arrange
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "test.webm",
                "audio/webm",
                "test audio content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/ava/voice")
                        .file(audioFile)
                        .param("conversationId", "test-conv")
                        .param("userId", "test-user"))
                .andExpect(status().isOk());
    }

    @Test
    void testVoiceChatEndpoint_WithoutAudio() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/ava/voice")
                        .param("conversationId", "test-conv")
                        .param("userId", "test-user"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCorsHeaders() throws Exception {
        mockMvc.perform(options("/ava/chat")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
