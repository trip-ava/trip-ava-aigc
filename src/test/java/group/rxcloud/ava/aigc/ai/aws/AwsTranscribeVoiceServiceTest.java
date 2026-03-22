package group.rxcloud.ava.aigc.ai.aws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AwsTranscribeVoiceService
 */
@ExtendWith(MockitoExtension.class)
public class AwsTranscribeVoiceServiceTest {

    @InjectMocks
    private AwsTranscribeVoiceService awsTranscribeVoiceService;

    @BeforeEach
    void setUp() {
        // Service is initialized via @PostConstruct
    }

    @Test
    void testLanguageConstant() {
        // Verify the language constant
        assertEquals("zh-CN", AwsTranscribeVoiceService.ZH);
    }

    @Test
    void testTranscript_DataClass() {
        // Arrange
        AwsTranscribeVoiceService.Transcript transcript = new AwsTranscribeVoiceService.Transcript();
        AwsTranscribeVoiceService.Result result = new AwsTranscribeVoiceService.Result();
        AwsTranscribeVoiceService.Item item = new AwsTranscribeVoiceService.Item();
        item.setTranscript("你好 AVA");

        // Act
        result.setTranscripts(java.util.Arrays.asList(item));
        transcript.setResults(result);

        // Assert
        assertNotNull(transcript.getResults());
        assertNotNull(transcript.getResults().getTranscripts());
        assertEquals(1, transcript.getResults().getTranscripts().size());
        assertEquals("你好 AVA", transcript.getResults().getTranscripts().get(0).getTranscript());
    }

    @Test
    void testTranscript_EmptyTranscript() {
        // Arrange
        AwsTranscribeVoiceService.Transcript transcript = new AwsTranscribeVoiceService.Transcript();

        // Act & Assert
        assertNull(transcript.getResults());
    }

    @Test
    void testTranscript_MultipleItems() {
        // Arrange
        AwsTranscribeVoiceService.Transcript transcript = new AwsTranscribeVoiceService.Transcript();
        AwsTranscribeVoiceService.Result result = new AwsTranscribeVoiceService.Result();

        AwsTranscribeVoiceService.Item item1 = new AwsTranscribeVoiceService.Item();
        item1.setTranscript("第一句话");

        AwsTranscribeVoiceService.Item item2 = new AwsTranscribeVoiceService.Item();
        item2.setTranscript("第二句话");

        // Act
        result.setTranscripts(java.util.Arrays.asList(item1, item2));
        transcript.setResults(result);

        // Assert
        assertEquals(2, transcript.getResults().getTranscripts().size());
        assertEquals("第一句话", transcript.getResults().getTranscripts().get(0).getTranscript());
        assertEquals("第二句话", transcript.getResults().getTranscripts().get(1).getTranscript());
    }

    @Test
    void testConvertURLToString_InvalidURL() {
        // Arrange
        String invalidUrl = "http://invalid-url-that-does-not-exist.com/result.json";

        // Act
        String result = awsTranscribeVoiceService.convertURLToString(invalidUrl);

        // Assert
        // Should return empty string for invalid URL
        assertNotNull(result);
    }

    @Test
    void testServiceInitialization() {
        // Verify service is created
        assertNotNull(awsTranscribeVoiceService);
    }
}
