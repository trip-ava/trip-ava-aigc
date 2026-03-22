package group.rxcloud.ava.aigc.controller;

import group.rxcloud.ava.aigc.ai.aws.AwsPollyVoiceService;
import group.rxcloud.ava.aigc.ai.aws.AwsTranscribeVoiceService;
import group.rxcloud.ava.aigc.service.OpenAiChatService;
import group.rxcloud.ava.aigc.utils.AwsS3Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AvaController
 */
@ExtendWith(MockitoExtension.class)
public class AvaControllerTest {

    @Mock
    private OpenAiChatService openAiChatService;

    @Mock
    private AwsTranscribeVoiceService awsTranscribeVoiceService;

    @Mock
    private AwsPollyVoiceService awsPollyVoiceService;

    @Mock
    private AwsS3Utils awsS3Utils;

    @InjectMocks
    private AvaController avaController;

    @BeforeEach
    void setUp() {
        // Setup is handled by Mockito annotations
    }

    @Test
    void testChat_Success() {
        // Arrange
        String userMessage = "Hello AVA";
        String expectedResponse = "Hi there! I'm AVA, your travel guide.";

        when(openAiChatService.chat(anyString(), isNull()))
                .thenReturn(expectedResponse);

        AvaController.ChatRequest request = new AvaController.ChatRequest();
        request.setUserId("user-1");
        request.setConversationId("conv-1");

        AvaController.ChatMessage message = new AvaController.ChatMessage();
        message.setRole("user");
        message.setContent(userMessage);
        message.setTimestamp(System.currentTimeMillis());
        request.setMessage(message);

        // Act
        AvaController.ChatResponse response = avaController.chat(request);

        // Assert
        assertNotNull(response);
        assertEquals("conv-1", response.getConversationId());
        assertNotNull(response.getMessage());
        assertEquals("assistant", response.getMessage().getRole());
        assertEquals(expectedResponse, response.getMessage().getContent());

        verify(openAiChatService, times(1)).chat(eq(userMessage), isNull());
    }

    @Test
    void testChat_WithHistory() {
        // Arrange
        String userMessage = "Tell me more";
        String expectedResponse = "Here's more information...";

        when(openAiChatService.chat(anyString(), anyList()))
                .thenReturn(expectedResponse);

        AvaController.ChatRequest request = new AvaController.ChatRequest();
        request.setUserId("user-1");
        request.setConversationId("conv-1");

        AvaController.ChatMessage message = new AvaController.ChatMessage();
        message.setRole("user");
        message.setContent(userMessage);
        message.setTimestamp(System.currentTimeMillis());
        request.setMessage(message);

        AvaController.ChatMessage historyMessage = new AvaController.ChatMessage();
        historyMessage.setRole("assistant");
        historyMessage.setContent("Previous message");
        historyMessage.setTimestamp(System.currentTimeMillis());
        request.setHistory(Arrays.asList(historyMessage));

        // Act
        AvaController.ChatResponse response = avaController.chat(request);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse, response.getMessage().getContent());

        verify(openAiChatService, times(1)).chat(eq(userMessage), anyList());
    }

    @Test
    void testVoiceChat_Success() throws Exception {
        // Arrange
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio", "test.webm", "audio/webm", "test audio content".getBytes());
        String transcribedText = "Hello AVA";
        String expectedResponse = "I received your voice message!";
        String audioUrl = "https://s3.amazonaws.com/bucket/voice/test.mp3";
        String voiceResponseUrl = "https://s3.amazonaws.com/bucket/voice-response/test.mp3";

        // Mock S3 upload
        when(awsS3Utils.uploadAndGetUrl(anyString(), any()))
                .thenReturn(audioUrl);

        // Mock Transcribe
        AwsTranscribeVoiceService.Transcript transcript = createMockTranscript(transcribedText);
        when(awsTranscribeVoiceService.transferMp3ToText(anyString(), any()))
                .thenReturn(transcript);

        // Mock Chat service
        when(openAiChatService.chat(eq(transcribedText), isNull()))
                .thenReturn(expectedResponse);

        // Mock Polly
        when(awsPollyVoiceService.genVoiceMp3FromText(anyString()))
                .thenReturn(new ByteArrayInputStream("voice data".getBytes()));

        // Mock S3 upload for voice response
        when(awsS3Utils.uploadStreamAndGetUrl(anyString(), any()))
                .thenReturn(voiceResponseUrl);

        // Act
        AvaController.ChatResponse response = avaController.voiceChat(
                audioFile, "conv-1", "user-1");

        // Assert
        assertNotNull(response);
        assertEquals("conv-1", response.getConversationId());
        assertNotNull(response.getMessage());
        assertEquals("assistant", response.getMessage().getRole());
        assertEquals(expectedResponse, response.getMessage().getContent());

        // Verify it's a voice response with URL
        assertTrue(response instanceof AvaController.ChatResponseWithVoice);
        AvaController.ChatResponseWithVoice voiceResponse = (AvaController.ChatResponseWithVoice) response;
        assertEquals(voiceResponseUrl, voiceResponse.getVoiceUrl());
        assertEquals(transcribedText, voiceResponse.getTranscribedText());
    }

    @Test
    void testVoiceChat_TranscriptionFailure() throws Exception {
        // Arrange
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio", "test.webm", "audio/webm", "test audio content".getBytes());
        String expectedResponse = "I received your message!";
        String audioUrl = "https://s3.amazonaws.com/bucket/voice/test.mp3";

        // Mock S3 upload
        when(awsS3Utils.uploadAndGetUrl(anyString(), any()))
                .thenReturn(audioUrl);

        // Mock Transcribe to throw exception
        when(awsTranscribeVoiceService.transferMp3ToText(anyString(), any()))
                .thenThrow(new RuntimeException("Transcription failed"));

        // Mock Chat service with fallback text
        when(openAiChatService.chat(eq("[语音识别失败，请重试]"), isNull()))
                .thenReturn(expectedResponse);

        // Mock Polly
        when(awsPollyVoiceService.genVoiceMp3FromText(anyString()))
                .thenReturn(new ByteArrayInputStream("voice data".getBytes()));

        // Mock S3 upload for voice response
        when(awsS3Utils.uploadStreamAndGetUrl(anyString(), any()))
                .thenReturn("https://s3.amazonaws.com/bucket/voice-response/test.mp3");

        // Act
        AvaController.ChatResponse response = avaController.voiceChat(
                audioFile, "conv-1", "user-1");

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse, response.getMessage().getContent());
    }

    @Test
    void testAvaHelloContent_Success() {
        // Arrange
        AvaController.AvaHelloRequest request = new AvaController.AvaHelloRequest();
        request.setUserId("user-1");

        // Act
        AvaController.AvaHelloResponse response = avaController.avaHelloContent(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().contains("AVA"));
        assertTrue(response.getContent().contains("旅行向导"));
    }

    @Test
    void testAvaHelloVoice_Success() throws Exception {
        // Arrange
        String content = "Hello AVA";
        String voiceUrl = "https://s3.amazonaws.com/bucket/voice-hello/test.mp3";

        AvaController.AvaHelloVoiceRequest request = new AvaController.AvaHelloVoiceRequest();
        request.setUserId("user-1");
        request.setContent(content);

        // Mock Polly
        when(awsPollyVoiceService.genVoiceMp3FromText(eq(content)))
                .thenReturn(new ByteArrayInputStream("voice data".getBytes()));

        // Mock S3 upload
        when(awsS3Utils.uploadStreamAndGetUrl(anyString(), any()))
                .thenReturn(voiceUrl);

        // Act
        AvaController.AvaHelloVoiceResponse response = avaController.avaHelloVoice(request);

        // Assert
        assertNotNull(response);
        assertEquals(voiceUrl, response.getVoiceUrl());

        verify(awsPollyVoiceService, times(1)).genVoiceMp3FromText(eq(content));
        verify(awsS3Utils, times(1)).uploadStreamAndGetUrl(anyString(), any());
    }

    @Test
    void testAvaHelloVoice_EmptyContent() {
        // Arrange
        AvaController.AvaHelloVoiceRequest request = new AvaController.AvaHelloVoiceRequest();
        request.setUserId("user-1");
        request.setContent("  ");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            avaController.avaHelloVoice(request);
        });
        assertEquals("Content cannot be empty", exception.getMessage());
    }

    @Test
    void testHealth() {
        // Act
        AvaController.HealthStatus response = avaController.health();

        // Assert
        assertNotNull(response);
        assertEquals("UP", response.getStatus());
        assertNotNull(response.getTimestamp());
        assertEquals("1.0.0", response.getVersion());
        assertNotNull(response.getComponents());
        assertNotNull(response.getComponents().getOpenAi());
        assertNotNull(response.getComponents().getAwsTranscribe());
        assertNotNull(response.getComponents().getAwsPolly());
        assertNotNull(response.getComponents().getAwsS3());
    }

    @Test
    void testHealthSimple() {
        // Act
        String response = avaController.healthSimple();

        // Assert
        assertEquals("OK", response);
    }

    @Test
    void testDuplicateRequestDetection() throws Exception {
        // Arrange
        String userId = "user-1";
        String content = "Same message";

        MockMultipartFile audioFile1 = new MockMultipartFile(
                "audio", "test.webm", "audio/webm", content.getBytes());
        MockMultipartFile audioFile2 = new MockMultipartFile(
                "audio", "test.webm", "audio/webm", content.getBytes());

        // Setup mocks for first request
        when(awsS3Utils.uploadAndGetUrl(anyString(), any()))
                .thenReturn("https://s3.amazonaws.com/bucket/voice/test.mp3");
        when(awsTranscribeVoiceService.transferMp3ToText(anyString(), any()))
                .thenReturn(createMockTranscript("Hello"));
        when(openAiChatService.chat(anyString(), isNull()))
                .thenReturn("Response");
        when(awsPollyVoiceService.genVoiceMp3FromText(anyString()))
                .thenReturn(new ByteArrayInputStream("voice".getBytes()));
        when(awsS3Utils.uploadStreamAndGetUrl(anyString(), any()))
                .thenReturn("https://s3.amazonaws.com/bucket/voice-response/test.mp3");

        // First request should succeed
        avaController.voiceChat(audioFile1, "conv-1", userId);

        // Second identical request should fail with duplicate error
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            avaController.voiceChat(audioFile2, "conv-1", userId);
        });
        assertTrue(exception.getMessage().contains("Duplicate request"));
    }

    @Test
    void testChat_NullHistory() {
        // Arrange
        String userMessage = "Hello";
        String expectedResponse = "Hi!";

        when(openAiChatService.chat(anyString(), isNull()))
                .thenReturn(expectedResponse);

        AvaController.ChatRequest request = new AvaController.ChatRequest();
        request.setUserId("user-1");
        request.setConversationId("conv-1");

        AvaController.ChatMessage message = new AvaController.ChatMessage();
        message.setRole("user");
        message.setContent(userMessage);
        message.setTimestamp(System.currentTimeMillis());
        request.setMessage(message);
        request.setHistory(null);

        // Act
        AvaController.ChatResponse response = avaController.chat(request);

        // Assert
        assertNotNull(response);
        verify(openAiChatService, times(1)).chat(eq(userMessage), isNull());
    }

    // Helper method to create mock transcript
    private AwsTranscribeVoiceService.Transcript createMockTranscript(String text) {
        AwsTranscribeVoiceService.Transcript transcript = new AwsTranscribeVoiceService.Transcript();
        AwsTranscribeVoiceService.Result result = new AwsTranscribeVoiceService.Result();
        AwsTranscribeVoiceService.Item item = new AwsTranscribeVoiceService.Item();
        item.setTranscript(text);
        result.setTranscripts(Arrays.asList(item));
        transcript.setResults(result);
        return transcript;
    }
}
