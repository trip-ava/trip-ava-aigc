package group.rxcloud.ava.aigc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Message entity for chat history
 */
@Data
@Entity
@Table(name = "messages")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(nullable = false, length = 50)
    private String role; // 'user' or 'assistant'

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "message_type", length = 20)
    private String messageType = "text"; // 'text', 'voice', 'image'

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "duration")
    private Integer duration; // For voice messages (seconds)

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * Check if message is from user
     */
    public boolean isUserMessage() {
        return "user".equalsIgnoreCase(role);
    }

    /**
     * Check if message is from assistant
     */
    public boolean isAssistantMessage() {
        return "assistant".equalsIgnoreCase(role);
    }
}
