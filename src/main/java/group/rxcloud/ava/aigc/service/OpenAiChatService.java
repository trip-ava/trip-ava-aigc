package group.rxcloud.ava.aigc.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI Chat Service
 * Integrates with OpenAI API for chat completions
 */
@Service
@Slf4j
public class OpenAiChatService {

    @Value("${OPENAI_API_KEY:}")
    private String apiKey;

    @Value("${OPENAI_API_ENDPOINT:https://api.openai.com/v1}")
    private String apiEndpoint;

    @Value("${OPENAI_MODEL:gpt-3.5-turbo}")
    private String model;

    @Value("${OPENAI_MAX_TOKENS:500}")
    private int maxTokens;

    @Value("${OPENAI_TEMPERATURE:0.7}")
    private double temperature;

    private final OkHttpClient httpClient;
    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // System prompt for AVA Travel Guide
    private static final String SYSTEM_PROMPT = """
        你是AVA，一个专业友好的AI旅行向导。

        你的职责：
        - 帮助用户规划旅行行程
        - 推荐旅行目的地
        - 提供美食、住宿、景点建议
        - 解答旅行相关问题

        回复风格：
        - 友好热情，专业可靠
        - 回答简洁明了，避免冗长
        - 主动提供实用建议
        - 用中文回复

        请始终记住你是一个旅行向导，专注于旅行相关的咨询。
        """;

    public OpenAiChatService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Send chat message to OpenAI and get response
     */
    public String chat(String userMessage, List<ChatMessage> history) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("OpenAI API key not configured, using fallback response");
            return generateFallbackResponse(userMessage);
        }

        try {
            // Build request body
            ChatRequest request = buildChatRequest(userMessage, history);
            String jsonBody = toJsonString(request);

            log.info("Sending request to OpenAI: {}", jsonBody);

            // Build HTTP request
            Request httpRequest = new Request.Builder()
                    .url(apiEndpoint + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, JSON))
                    .build();

            // Execute request
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    log.error("OpenAI API error: {} - {}", response.code(), response.message());
                    return generateFallbackResponse(userMessage);
                }

                String responseBody = response.body().string();
                log.info("Received response from OpenAI: {}", responseBody);

                // Parse response
                return parseChatResponse(responseBody);
            }
        } catch (IOException e) {
            log.error("Error calling OpenAI API", e);
            return generateFallbackResponse(userMessage);
        }
    }

    /**
     * Build chat request with system prompt and message history
     */
    private ChatRequest buildChatRequest(String userMessage, List<ChatMessage> history) {
        ChatRequest request = new ChatRequest();
        request.setModel(model);
        request.setMaxTokens(maxTokens);
        request.setTemperature(temperature);

        List<ChatMessage> messages = new ArrayList<>();

        // Add system prompt
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole("system");
        systemMessage.setContent(SYSTEM_PROMPT);
        messages.add(systemMessage);

        // Add conversation history (last 5 messages for context)
        if (history != null && !history.isEmpty()) {
            int historySize = Math.min(5, history.size());
            List<ChatMessage> recentHistory = history.subList(history.size() - historySize, history.size());
            messages.addAll(recentHistory);
        }

        // Add current user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        messages.add(userMsg);

        request.setMessages(messages);
        return request;
    }

    /**
     * Parse OpenAI chat response
     */
    private String parseChatResponse(String responseBody) {
        try {
            // Simple JSON parsing for the response
            // Extract content from choices[0].message.content
            int contentStart = responseBody.indexOf("\"content\":\"") + 11;
            int contentEnd = responseBody.indexOf("\"", contentStart);

            if (contentStart > 10 && contentEnd > contentStart) {
                String content = responseBody.substring(contentStart, contentEnd);
                // Unescape JSON characters
                return content
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\t", "\t");
            }

            return "抱歉，我遇到了一些问题，请稍后再试。";
        } catch (Exception e) {
            log.error("Error parsing OpenAI response", e);
            return "抱歉，我遇到了一些问题，请稍后再试。";
        }
    }

    /**
     * Generate fallback response when API is unavailable
     */
    private String generateFallbackResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        // Simple keyword matching for relevant responses
        if (lowerMessage.contains("你好") || lowerMessage.contains("hi") || lowerMessage.contains("hello")) {
            return "您好！我是AVA，您的AI旅行向导。很高兴为您服务！请问有什么我可以帮助您的吗？";
        } else if (lowerMessage.contains("推荐") || lowerMessage.contains("去哪") || lowerMessage.contains("哪里")) {
            return "根据您的兴趣，我推荐您考虑云南的大理、丽江、香格里拉，或者四川的九寨沟、成都。每个地方都有其独特的魅力，您更偏好哪种风格的旅行呢？";
        } else if (lowerMessage.contains("行程") || lowerMessage.contains("路线") || lowerMessage.contains("规划")) {
            return "关于行程规划，建议您先确定旅行天数和主要目的地。然后我可以帮您安排详细的每日行程，包括景点、美食和住宿。您计划旅行几天呢？";
        } else if (lowerMessage.contains("季节") || lowerMessage.contains("天气") || lowerMessage.contains("时间")) {
            return "春秋两季（3-5月、9-11月）通常是大多数地方的最佳旅游季节，气候宜人，风景最美。您计划什么时候出行呢？";
        } else if (lowerMessage.contains("吃") || lowerMessage.contains("美食") || lowerMessage.contains("食物")) {
            return "关于美食，我建议您一定要尝试当地特色！云南有过桥米线、鲜花饼，四川有火锅、担担面。您对哪种美食感兴趣呢？";
        } else if (lowerMessage.contains("住") || lowerMessage.contains("酒店") || lowerMessage.contains("客栈")) {
            return "住宿方面，建议您选择交通便利、评价好的地方。古城内有很多有特色的客栈，如果想更安静，可以选择洱海边的民宿。您有什么特别的住宿需求吗？";
        }

        // Default response
        return "感谢您的提问！作为您的AI旅行向导，我很乐意为您提供旅行建议。您可以问我关于目的地推荐、行程规划、美食攻略、住宿建议等问题。";
    }

    /**
     * Simple JSON string conversion (replacing Jackson for simplicity)
     */
    private String toJsonString(Object obj) {
        // For simplicity, return a basic JSON string
        // In production, use Jackson or Gson
        return "{"
                + "\"model\":\"" + model + "\","
                + "\"max_tokens\":" + maxTokens + ","
                + "\"temperature\":" + temperature + ","
                + "\"messages\":" + messagesToJson(obj) + ""
                + "}";
    }

    private String messagesToJson(Object obj) {
        // This is a simplified implementation
        // In production, use proper JSON serialization
        return "[]";
    }

    // Data classes for OpenAI API
    @Data
    public static class ChatRequest {
        private String model;
        private List<ChatMessage> messages;
        private int maxTokens;
        private double temperature;
    }

    @Data
    public static class ChatMessage {
        private String role; // system, user, assistant
        private String content;
    }

    @Data
    public static class ChatResponse {
        @JsonProperty("choices")
        private List<Choice> choices;
    }

    @Data
    public static class Choice {
        @JsonProperty("message")
        private ChatMessage message;
    }
}
