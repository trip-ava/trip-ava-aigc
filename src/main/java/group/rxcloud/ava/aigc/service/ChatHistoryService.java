package group.rxcloud.ava.aigc.service;

import group.rxcloud.ava.aigc.dto.chat.*;
import group.rxcloud.ava.aigc.entity.Conversation;
import group.rxcloud.ava.aigc.entity.Message;
import group.rxcloud.ava.aigc.entity.User;
import group.rxcloud.ava.aigc.repository.ConversationRepository;
import group.rxcloud.ava.aigc.repository.MessageRepository;
import group.rxcloud.ava.aigc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing chat history in database
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Create a new conversation for user
     */
    @Transactional
    public ConversationResponse createConversation(Long userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String conversationId = generateConversationId();

        Conversation conversation = Conversation.builder()
                .conversationId(conversationId)
                .user(user)
                .title(title != null ? title : "New Conversation")
                .isActive(true)
                .build();

        Conversation saved = conversationRepository.save(conversation);
        log.info("Created conversation {} for user {}", conversationId, userId);

        return mapToConversationResponse(saved);
    }

    /**
     * Get conversation by ID
     */
    @Transactional(readOnly = true)
    public Optional<ConversationResponse> getConversation(String conversationId, Long userId) {
        return conversationRepository.findByConversationIdAndUserId(conversationId, userId)
                .map(this::mapToConversationResponse);
    }

    /**
     * Get all conversations for user
     */
    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository
                .findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(userId);

        return conversations.stream()
                .map(this::mapToConversationSummary)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated conversations for user
     */
    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> getUserConversations(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return conversationRepository.findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(userId, pageable)
                .getContent()
                .stream()
                .map(this::mapToConversationSummary)
                .collect(Collectors.toList());
    }

    /**
     * Save a message to conversation
     */
    @Transactional
    public void saveMessage(Long userId, ChatHistoryRequest request) {
        Conversation conversation = conversationRepository
                .findByConversationIdAndUserId(request.getConversationId(), userId)
                .orElseGet(() -> {
                    // Create conversation if not exists
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return createNewConversation(user, request.getConversationId());
                });

        Message message = Message.builder()
                .conversation(conversation)
                .role(request.getRole())
                .content(request.getContent())
                .messageType(request.getMessageType())
                .audioUrl(request.getAudioUrl())
                .imageUrl(request.getImageUrl())
                .duration(request.getDuration())
                .timestamp(LocalDateTime.now())
                .build();

        conversation.addMessage(message);
        conversationRepository.save(conversation);

        log.debug("Saved message to conversation {} for user {}",
                request.getConversationId(), userId);
    }

    /**
     * Get messages for conversation
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse.MessageResponse> getMessages(String conversationId, Long userId) {
        Conversation conversation = conversationRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        return conversation.getMessages().stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update conversation title
     */
    @Transactional
    public void updateConversationTitle(String conversationId, Long userId, String newTitle) {
        Conversation conversation = conversationRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setTitle(newTitle);
        conversationRepository.save(conversation);

        log.info("Updated conversation {} title to '{}'", conversationId, newTitle);
    }

    /**
     * Soft delete conversation
     */
    @Transactional
    public void deleteConversation(String conversationId, Long userId) {
        Conversation conversation = conversationRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setIsActive(false);
        conversationRepository.save(conversation);

        log.info("Soft deleted conversation {} for user {}", conversationId, userId);
    }

    /**
     * Search conversations by keyword
     */
    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> searchConversations(Long userId, String keyword) {
        List<Conversation> conversations = conversationRepository
                .searchByUserIdAndKeyword(userId, keyword);

        return conversations.stream()
                .map(this::mapToConversationSummary)
                .collect(Collectors.toList());
    }

    /**
     * Search messages by keyword
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse.MessageResponse> searchMessages(Long userId, String keyword) {
        List<Message> messages = messageRepository
                .searchByUserIdAndContentContaining(userId, keyword);

        return messages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get conversation statistics for user
     */
    @Transactional(readOnly = true)
    public ConversationStats getConversationStats(Long userId) {
        long totalConversations = conversationRepository.countByUserIdAndIsActiveTrue(userId);

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        long recentConversations = conversationRepository.countRecentConversations(userId, oneWeekAgo);

        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        long recentMessages = messageRepository.countRecentMessages(userId, oneDayAgo);

        return ConversationStats.builder()
                .totalConversations(totalConversations)
                .recentConversations(recentConversations)
                .recentMessages(recentMessages)
                .build();
    }

    /**
     * Generate unique conversation ID
     */
    private String generateConversationId() {
        return "conv-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Create new conversation entity
     */
    private Conversation createNewConversation(User user, String conversationId) {
        Conversation conversation = Conversation.builder()
                .conversationId(conversationId)
                .user(user)
                .title("New Conversation")
                .isActive(true)
                .build();
        return conversationRepository.save(conversation);
    }

    /**
     * Map Conversation entity to Response DTO
     */
    private ConversationResponse mapToConversationResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .conversationId(conversation.getConversationId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .messages(conversation.getMessages().stream()
                        .map(this::mapToMessageResponse)
                        .collect(Collectors.toList()))
                .messageCount(conversation.getMessages().size())
                .build();
    }

    /**
     * Map Conversation entity to Summary Response DTO
     */
    private ConversationSummaryResponse mapToConversationSummary(Conversation conversation) {
        String lastMessagePreview = conversation.getMessages().isEmpty()
                ? ""
                : truncateText(conversation.getMessages()
                        .get(conversation.getMessages().size() - 1).getContent(), 50);

        return ConversationSummaryResponse.builder()
                .conversationId(conversation.getConversationId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .messageCount(conversation.getMessages().size())
                .lastMessagePreview(lastMessagePreview)
                .build();
    }

    /**
     * Map Message entity to Response DTO
     */
    private ConversationResponse.MessageResponse mapToMessageResponse(Message message) {
        return ConversationResponse.MessageResponse.builder()
                .role(message.getRole())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .messageType(message.getMessageType())
                .audioUrl(message.getAudioUrl())
                .imageUrl(message.getImageUrl())
                .duration(message.getDuration())
                .build();
    }

    /**
     * Truncate text to specified length
     */
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * Conversation statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ConversationStats {
        private long totalConversations;
        private long recentConversations;
        private long recentMessages;
    }
}
