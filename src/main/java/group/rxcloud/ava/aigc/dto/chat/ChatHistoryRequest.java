package group.rxcloud.ava.aigc.dto.chat;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Request DTO for saving chat message
 */
@Data
public class ChatHistoryRequest {

    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    @NotBlank(message = "Role is required")
    private String role; // 'user' or 'assistant'

    @NotBlank(message = "Content is required")
    private String content;

    private String messageType = "text"; // 'text', 'voice', 'image'

    private String audioUrl;

    private String imageUrl;

    private Integer duration;
}
