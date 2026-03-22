package group.rxcloud.ava.aigc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OpenAiChatService
 */
@ExtendWith(MockitoExtension.class)
public class OpenAiChatServiceTest {

    private OpenAiChatService openAiChatService;

    @BeforeEach
    void setUp() {
        openAiChatService = new OpenAiChatService();
        // Set API key for testing
        ReflectionTestUtils.setField(openAiChatService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(openAiChatService, "apiEndpoint", "https://api.openai.com/v1");
        ReflectionTestUtils.setField(openAiChatService, "model", "gpt-3.5-turbo");
        ReflectionTestUtils.setField(openAiChatService, "maxTokens", 500);
        ReflectionTestUtils.setField(openAiChatService, "temperature", 0.7);
    }

    @Test
    void testFallbackResponse_Greeting() {
        // Arrange
        ReflectionTestUtils.setField(openAiChatService, "apiKey", "");
        String userMessage = "你好";

        // Act
        String response = openAiChatService.chat(userMessage, null);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("AVA"));
        assertTrue(response.contains("旅行向导"));
    }

    @Test
    void testFallbackResponse_Recommendation() {
        // Arrange
        ReflectionTestUtils.setField(openAiChatService, "apiKey", "");
        String userMessage = "推荐一些地方";

        // Act
        String response = openAiChatService.chat(userMessage, null);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("推荐") || response.contains("云南") || response.contains("大理"));
    }

    @Test
    void testFallbackResponse_Itinerary() {
        // Arrange
        ReflectionTestUtils.setField(openAiChatService, "apiKey", "");
        String userMessage = "帮我规划行程";

        // Act
        String response = openAiChatService.chat(userMessage, null);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("行程") || response.contains("规划"));
    }

    @Test
    void testFallbackResponse_Season() {
        // Arrange
        ReflectionTestUtils.setField(openAiChatService, "apiKey", "");
        String userMessage = "什么季节去最好";

        // Act
        String response = openAiChatService.chat(userMessage, null);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("季节") || response.contains("春秋"));
    }

    @Test
    void testFallbackResponse_Food() {
        // Arrange
        ReflectionTestUtils.setField(openAiChatService, "apiKey", "");
        String userMessage = "有什么美食";

        // Act
        String response = openAiChatService.chat(userMessage, null);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("美食") || response.contains("米线"));
    }

    @Test
    void testFallbackResponse_Hotel() {
        // Arrange
        ReflectionTestUtils.setField(openAiChatService, "apiKey", "");
        String userMessage = "hotel";

        // Act
        String response = openAiChatService.chat(userMessage, null);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testFallbackResponse_Default() {
        // Arrange
        ReflectionTestUtils.setField(openAiChatService, "apiKey", "");
        String userMessage = "random unknown message";

        // Act
        String response = openAiChatService.chat(userMessage, null);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("AVA") || response.contains("旅行向导"));
    }

    @Test
    void testChatMessage_DataClass() {
        // Arrange
        OpenAiChatService.ChatMessage message = new OpenAiChatService.ChatMessage();

        // Act
        message.setRole("user");
        message.setContent("Hello");

        // Assert
        assertEquals("user", message.getRole());
        assertEquals("Hello", message.getContent());
    }

    @Test
    void testChatRequest_DataClass() {
        // Arrange
        OpenAiChatService.ChatRequest request = new OpenAiChatService.ChatRequest();
        OpenAiChatService.ChatMessage message = new OpenAiChatService.ChatMessage();
        message.setRole("user");
        message.setContent("Hello");

        // Act
        request.setModel("gpt-3.5-turbo");
        request.setMessages(Arrays.asList(message));
        request.setMaxTokens(100);
        request.setTemperature(0.5);

        // Assert
        assertEquals("gpt-3.5-turbo", request.getModel());
        assertNotNull(request.getMessages());
        assertEquals(1, request.getMessages().size());
        assertEquals(100, request.getMaxTokens());
        assertEquals(0.5, request.getTemperature());
    }

    @Test
    void testChatResponse_DataClass() {
        // Arrange
        OpenAiChatService.ChatResponse response = new OpenAiChatService.ChatResponse();
        OpenAiChatService.Choice choice = new OpenAiChatService.Choice();
        OpenAiChatService.ChatMessage message = new OpenAiChatService.ChatMessage();
        message.setRole("assistant");
        message.setContent("Hello!");
        choice.setMessage(message);

        // Act
        response.setChoices(Arrays.asList(choice));

        // Assert
        assertNotNull(response.getChoices());
        assertEquals(1, response.getChoices().size());
        assertEquals("assistant", response.getChoices().get(0).getMessage().getRole());
    }

    @Test
    void testParseChatResponse_ValidResponse() {
        // Arrange
        String jsonResponse = "{\"choices\":[{\"message\":{\"content\":\"Hello there!\"}}]}";

        // Act - using reflection to test private method
        String result = (String) ReflectionTestUtils.invokeMethod(
                openAiChatService, "parseChatResponse", jsonResponse);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testParseChatResponse_InvalidResponse() {
        // Arrange
        String jsonResponse = "{\"invalid\": \"response\"}";

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(
                openAiChatService, "parseChatResponse", jsonResponse);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("抱歉") || result.contains("问题"));
    }
}
