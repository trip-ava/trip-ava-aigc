package group.rxcloud.ava.aigc.controller;

import group.rxcloud.ava.aigc.ai.aws.AwsPollyVoiceService;
import group.rxcloud.ava.aigc.ai.aws.AwsTranscribeVoiceService;
import group.rxcloud.ava.aigc.service.OpenAiChatService;
import group.rxcloud.ava.aigc.utils.AwsS3Utils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@RestController(value = "/ava")
@Slf4j
@CrossOrigin(origins = "*")
public class AvaController {

    @Resource
    private OpenAiChatService openAiChatService;

    @Resource
    private AwsTranscribeVoiceService awsTranscribeVoiceService;

    @Resource
    private AwsPollyVoiceService awsPollyVoiceService;

    @Resource
    private AwsS3Utils awsS3Utils;

    // Thread pool for async voice processing
    private final ExecutorService voiceExecutor = Executors.newFixedThreadPool(4);

    // Request deduplication cache (simple in-memory, replace with Redis in production)
    private final ConcurrentHashMap<String, Long> recentRequests = new ConcurrentHashMap<>();
    private static final long DUPLICATE_WINDOW_MS = 5000; // 5 seconds

    // -- ava chat
    /*
     * ava chat with user
     */

    @Data
    public static class ChatMessage {
        private String role;    // "user" or "assistant"
        private String content;
        private Long timestamp;
    }

    @Data
    public static class ChatRequest {
        private String userId;
        private String conversationId;
        private ChatMessage message;
        private List<ChatMessage> history;
    }

    @Data
    public static class ChatResponse {
        private String conversationId;
        private ChatMessage message;
    }

    @Data
    public static class ChatResponseWithVoice extends ChatResponse {
        private String voiceUrl;
        private String transcribedText;
    }

    /**
     * Chat endpoint for AVA AI assistant
     * i: user message + conversation history
     * p: llm generate response
     * o: assistant response
     */
    @PostMapping(path = "/chat")
    public @ResponseBody ChatResponse chat(@RequestBody ChatRequest request) {
        log.info("Received chat request from user: {}, message: {}",
                request.getUserId(), request.getMessage().getContent());

        String userMessage = request.getMessage().getContent();
        List<OpenAiChatService.ChatMessage> history = convertHistory(request.getHistory());

        // Call OpenAI service
        String responseContent = openAiChatService.chat(userMessage, history);

        // Build response
        ChatResponse response = new ChatResponse();
        response.setConversationId(request.getConversationId());

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(responseContent);
        assistantMessage.setTimestamp(System.currentTimeMillis());

        response.setMessage(assistantMessage);

        log.info("Sending chat response: {}", responseContent);
        return response;
    }

    /**
     * Voice chat endpoint - combines speech-to-text, chat, and optional text-to-speech
     * Implements AWS Transcribe for STT and AWS Polly for TTS
     */
    @PostMapping(path = "/voice")
    public @ResponseBody ChatResponse voiceChat(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @RequestParam(value = "userId", required = false, defaultValue = "anonymous") String userId
    ) {
        String requestId = userId + "-" + System.currentTimeMillis();
        log.info("[{}] Received voice chat request from user: {}, file size: {}",
                requestId, userId, audioFile.getSize());

        // Check for duplicate requests
        if (isDuplicateRequest(userId, audioFile.getOriginalFilename())) {
            log.warn("[{}] Duplicate request detected, rejecting", requestId);
            throw new IllegalStateException("Duplicate request detected. Please wait before retrying.");
        }

        try {
            // Step 1: Save audio file temporarily
            Path tempFile = Files.createTempFile("voice-", ".webm");
            audioFile.transferTo(tempFile.toFile());
            log.info("[{}] Saved audio file to: {}", requestId, tempFile);

            // Step 2: Upload to S3 for transcription
            String s3Key = "voice/" + userId + "-" + UUID.randomUUID() + ".webm";
            String audioUrl = awsS3Utils.uploadAndGetUrl(s3Key, tempFile.toFile());
            log.info("[{}] Uploaded audio to S3: {}", requestId, audioUrl);

            // Step 3: Transcribe audio using AWS Transcribe
            String transcribedText;
            try {
                AwsTranscribeVoiceService.Transcript transcript =
                    awsTranscribeVoiceService.transferMp3ToText(s3Key, tempFile.toFile());
                transcribedText = extractTranscriptText(transcript);
                log.info("[{}] Transcribed text: {}", requestId, transcribedText);
            } catch (Exception e) {
                log.error("[{}] Transcription failed: {}", requestId, e.getMessage());
                transcribedText = "[语音识别失败，请重试]";
            }

            // Clean up temp file
            Files.deleteIfExists(tempFile);

            // Step 4: Call chat service with transcribed text
            String responseContent = openAiChatService.chat(transcribedText, null);
            log.info("[{}] AI response: {}", requestId, responseContent);

            // Step 5: Generate voice response using AWS Polly
            String voiceUrl = null;
            try {
                InputStream voiceStream = awsPollyVoiceService.genVoiceMp3FromText(responseContent);
                String voiceS3Key = "voice-response/" + userId + "-" + UUID.randomUUID() + ".mp3";
                voiceUrl = awsS3Utils.uploadStreamAndGetUrl(voiceS3Key, voiceStream);
                log.info("[{}] Generated voice URL: {}", requestId, voiceUrl);
            } catch (Exception e) {
                log.error("[{}] Voice generation failed: {}", requestId, e.getMessage());
                // Continue without voice URL
            }

            // Build response
            ChatResponse response = new ChatResponse();
            response.setConversationId(conversationId);

            ChatMessage assistantMessage = new ChatMessage();
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(responseContent);
            assistantMessage.setTimestamp(System.currentTimeMillis());

            response.setMessage(assistantMessage);

            // Add voice URL to response (extend response to include it)
            ChatResponseWithVoice voiceResponse = new ChatResponseWithVoice();
            voiceResponse.setConversationId(conversationId);
            voiceResponse.setMessage(assistantMessage);
            voiceResponse.setVoiceUrl(voiceUrl);
            voiceResponse.setTranscribedText(transcribedText);

            log.info("[{}] Voice chat completed successfully", requestId);
            return voiceResponse;

        } catch (IOException e) {
            log.error("[{}] Error processing voice chat: {}", requestId, e.getMessage());
            throw new RuntimeException("Failed to process voice chat", e);
        }
    }

    // -- ava hello
    /*
     * ava say 'hello content' to user
     */

    @Data
    public static class AvaHelloRequest {
        private String userId;
    }

    @Data
    public static class AvaHelloResponse {
        private String content;
    }

    /**
     * i: base user info
     * p: get user trip info -> llm gen content
     * o: hello content
     */
    @PostMapping(path = "/hello")
    public @ResponseBody AvaHelloResponse avaHelloContent(@RequestBody AvaHelloRequest request) {
        String userId = request.getUserId();
        log.info("Received hello request from user: {}", userId);

        AvaHelloResponse avaHelloResponse = new AvaHelloResponse();
        avaHelloResponse.setContent("你好！我是AVA，您的AI旅行向导。很高兴为您服务！请问有什么我可以帮助您的吗？");
        return avaHelloResponse;
    }

    @Data
    public static class AvaHelloVoiceRequest {
        private String userId;
        private String content;
    }

    @Data
    public static class AvaHelloVoiceResponse {
        private String voiceUrl;
    }

    /**
     * i: base user info + content
     * p: content -> voice using AWS Polly
     * o: voice url
     */
    @PostMapping(path = "/hello/voice")
    public @ResponseBody AvaHelloVoiceResponse avaHelloVoice(@RequestBody AvaHelloVoiceRequest request) {
        String userId = request.getUserId();
        String content = request.getContent();

        String requestId = userId + "-" + System.currentTimeMillis();
        log.info("[{}] Received voice synthesis request from user: {}, content length: {}",
                requestId, userId, content != null ? content.length() : 0);

        // Validate request
        if (content == null || content.trim().isEmpty()) {
            log.warn("[{}] Empty content received", requestId);
            throw new IllegalArgumentException("Content cannot be empty");
        }

        // Check for duplicate requests
        if (isDuplicateRequest(userId, content)) {
            log.warn("[{}] Duplicate voice synthesis request detected, rejecting", requestId);
            throw new IllegalStateException("Duplicate request detected. Please wait before retrying.");
        }

        try {
            // Generate voice using AWS Polly
            InputStream voiceStream = awsPollyVoiceService.genVoiceMp3FromText(content);

            // Upload to S3
            String voiceS3Key = "voice-hello/" + userId + "-" + UUID.randomUUID() + ".mp3";
            String voiceUrl = awsS3Utils.uploadStreamAndGetUrl(voiceS3Key, voiceStream);

            log.info("[{}] Voice synthesis completed, URL: {}", requestId, voiceUrl);

            AvaHelloVoiceResponse response = new AvaHelloVoiceResponse();
            response.setVoiceUrl(voiceUrl);
            return response;

        } catch (Exception e) {
            log.error("[{}] Voice synthesis failed: {}", requestId, e.getMessage());
            throw new RuntimeException("Voice synthesis failed", e);
        }
    }

    // -- Health check and monitoring endpoints

    @Data
    public static class HealthStatus {
        private String status;
        private long timestamp;
        private String version;
        private ComponentStatus components;
    }

    @Data
    public static class ComponentStatus {
        private String openAi;
        private String awsTranscribe;
        private String awsPolly;
        private String awsS3;
    }

    /**
     * Health check endpoint with detailed status
     */
    @GetMapping(path = "/health")
    public @ResponseBody HealthStatus health() {
        HealthStatus status = new HealthStatus();
        status.setStatus("UP");
        status.setTimestamp(System.currentTimeMillis());
        status.setVersion("1.0.0");

        ComponentStatus components = new ComponentStatus();
        components.setOpenAi(openAiChatService != null ? "UP" : "DOWN");
        components.setAwsTranscribe(awsTranscribeVoiceService != null ? "UP" : "DOWN");
        components.setAwsPolly(awsPollyVoiceService != null ? "UP" : "DOWN");
        components.setAwsS3(awsS3Utils != null ? "UP" : "DOWN");
        status.setComponents(components);

        return status;
    }

    /**
     * Simple health check for load balancers
     */
    @GetMapping(path = "/health/simple")
    public @ResponseBody String healthSimple() {
        return "OK";
    }

    // -- Helper methods

    /**
     * Check for duplicate requests to prevent double submission
     */
    private boolean isDuplicateRequest(String userId, String content) {
        if (content == null) return false;

        String key = userId + ":" + content.hashCode();
        Long lastRequest = recentRequests.get(key);
        long now = System.currentTimeMillis();

        if (lastRequest != null && (now - lastRequest) < DUPLICATE_WINDOW_MS) {
            return true;
        }

        recentRequests.put(key, now);

        // Clean up old entries periodically
        if (recentRequests.size() > 1000) {
            recentRequests.entrySet().removeIf(entry ->
                (now - entry.getValue()) > DUPLICATE_WINDOW_MS);
        }

        return false;
    }

    /**
     * Extract transcript text from AWS Transcribe response
     */
    private String extractTranscriptText(AwsTranscribeVoiceService.Transcript transcript) {
        if (transcript == null || transcript.getResults() == null) {
            return "";
        }

        StringBuilder text = new StringBuilder();
        if (transcript.getResults().getTranscripts() != null) {
            for (AwsTranscribeVoiceService.Item item : transcript.getResults().getTranscripts()) {
                if (item.getTranscript() != null) {
                    text.append(item.getTranscript());
                }
            }
        }
        return text.toString().trim();
    }

    /**
     * Convert frontend ChatMessage to service ChatMessage
     */
    private List<OpenAiChatService.ChatMessage> convertHistory(List<ChatMessage> frontendHistory) {
        if (frontendHistory == null || frontendHistory.isEmpty()) {
            return null;
        }

        List<OpenAiChatService.ChatMessage> serviceHistory = new java.util.ArrayList<>();
        for (ChatMessage msg : frontendHistory) {
            OpenAiChatService.ChatMessage serviceMsg = new OpenAiChatService.ChatMessage();
            serviceMsg.setRole(msg.getRole());
            serviceMsg.setContent(msg.getContent());
            serviceHistory.add(serviceMsg);
        }
        return serviceHistory;
    }
}
