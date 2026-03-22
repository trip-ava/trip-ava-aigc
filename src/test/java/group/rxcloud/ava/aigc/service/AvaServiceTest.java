package group.rxcloud.ava.aigc.service;

import group.rxcloud.ava.aigc.ai.aws.AwsPollyVoiceService;
import group.rxcloud.ava.aigc.ai.aws.AwsTranscribeVoiceService;
import group.rxcloud.ava.aigc.utils.AwsS3Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AvaService
 */
@ExtendWith(MockitoExtension.class)
public class AvaServiceTest {

    @Mock
    private OpenAiChatService openAiChatService;

    @Mock
    private AwsTranscribeVoiceService awsTranscribeVoiceService;

    @Mock
    private AwsPollyVoiceService awsPollyVoiceService;

    @Mock
    private AwsS3Utils awsS3Utils;

    @InjectMocks
    private AvaService avaService;

    @BeforeEach
    void setUp() {
        // Setup is handled by Mockito annotations
    }

    @Test
    void testProcessTextMessage_Success() {
        // Arrange
        String userMessage = "Hello AVA";
        String expectedResponse = "Hi there! I'm AVA.";
        String conversationId = "conv-1";
        String userId = "user-1";

        when(openAiChatService.chat(eq(userMessage), isNull()))
                .thenReturn(expectedResponse);

        // Act
        AvaService.ChatResponse response = avaService.processTextMessage(userMessage, conversationId, userId);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse, response.getContent());
        assertEquals(conversationId, response.getConversationId());
        verify(openAiChatService, times(1)).chat(eq(userMessage), isNull());
    }

    @Test
    void testProcessTextMessage_WithHistory() {
        // Arrange
        String userMessage = "Tell me more";
        String expectedResponse = "Here's more information...";
        String conversationId = "conv-1";
        String userId = "user-1";

        when(openAiChatService.chat(eq(userMessage), anyList()))
                .thenReturn(expectedResponse);

        // Act
        AvaService.ChatResponse response = avaService.processTextMessage(userMessage, conversationId, userId);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse, response.getContent());
    }

    @Test
    void testProcessVoiceMessage_Success() throws Exception {
        // Arrange
        File audioFile = new File("test.mp3");
        String transcribedText = "Hello AVA";
        String expectedResponse = "I received your voice message!";
        String conversationId = "conv-1";
        String userId = "user-1";

        // Mock Transcribe
        AwsTranscribeVoiceService.Transcript transcript = createMockTranscript(transcribedText);
        when(awsTranscribeVoiceService.transferMp3ToText(anyString(), any(File.class)))
                .thenReturn(transcript);

        // Mock Chat service
        when(openAiChatService.chat(eq(transcribedText), isNull()))
                .thenReturn(expectedResponse);

        // Act
        AvaService.VoiceChatResponse response = avaService.processVoiceMessage(audioFile, conversationId, userId);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse, response.getContent());
        assertEquals(transcribedText, response.getTranscribedText());
    }

    @Test
    void testProcessVoiceMessage_TranscriptionFailed() throws Exception {
        // Arrange
        File audioFile = new File("test.mp3");
        String conversationId = "conv-1";
        String userId = "user-1";

        // Mock Transcribe to throw exception
        when(awsTranscribeVoiceService.transferMp3ToText(anyString(), any(File.class)))
                .thenThrow(new RuntimeException("Transcription failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            avaService.processVoiceMessage(audioFile, conversationId, userId);
        });
    }

    @Test
    void testGenerateVoiceResponse_Success() throws Exception {
        // Arrange
        String content = "Hello from AVA";
        String voiceUrl = "https://s3.amazonaws.com/bucket/voice/test.mp3";

        when(awsPollyVoiceService.genVoiceMp3FromText(eq(content)))
                .thenReturn(new ByteArrayInputStream("voice data".getBytes()));

        when(awsS3Utils.uploadStreamAndGetUrl(anyString(), any()))
                .thenReturn(voiceUrl);

        // Act
        String result = avaService.generateVoiceResponse(content);

        // Assert
        assertNotNull(result);
        assertEquals(voiceUrl, result);
        verify(awsPollyVoiceService, times(1)).genVoiceMp3FromText(eq(content));
        verify(awsS3Utils, times(1)).uploadStreamAndGetUrl(anyString(), any());
    }

    @Test
    void testGenerateGreeting() {
        // Act
        String greeting = avaService.generateGreeting("user-1");

        // Assert
        assertNotNull(greeting);
        assertTrue(greeting.contains("AVA"));
        assertTrue(greeting.contains("旅行向导"));
    }

    @Test
    void testGenerateGreeting_NullUserId() {
        // Act
        String greeting = avaService.generateGreeting(null);

        // Assert
        assertNotNull(greeting);
        assertTrue(greeting.contains("AVA"));
    }

    @Test
    void testHealthCheck() {
        // Act
        AvaService.HealthStatus status = avaService.healthCheck();

        // Assert
        assertNotNull(status);
        assertEquals("UP", status.getStatus());
    }

    // Helper method to create mock transcript
    private AwsTranscribeVoiceService.Transcript createMockTranscript(String text) {
        AwsTranscribeVoiceService.Transcript transcript = new AwsTranscribeVoiceService.Transcript();
        AwsTranscribeVoiceService.Result result = new AwsTranscribeVoiceService.Result();
        AwsTranscribeVoiceService.Item item = new AwsTranscribeVoiceService.Item();
        item.setTranscript(text);
        result.setTranscripts(java.util.Arrays.asList(item));
        transcript.setResults(result);
        return transcript;
    }
}
