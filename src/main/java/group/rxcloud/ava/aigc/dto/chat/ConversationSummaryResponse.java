package group.rxcloud.ava.aigc.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for conversation summary (list view)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryResponse {

    private String conversationId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int messageCount;
    private String lastMessagePreview;
}
