package group.rxcloud.ava.aigc.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for conversation with messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private String conversationId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MessageResponse> messages;
    private int messageCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String role;
        private String content;
        private LocalDateTime timestamp;
        private String messageType;
        private String audioUrl;
        private String imageUrl;
        private Integer duration;
    }
}
