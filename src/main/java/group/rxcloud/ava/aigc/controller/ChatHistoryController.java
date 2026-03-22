package group.rxcloud.ava.aigc.controller;

import group.rxcloud.ava.aigc.dto.chat.*;
import group.rxcloud.ava.aigc.entity.User;
import group.rxcloud.ava.aigc.repository.UserRepository;
import group.rxcloud.ava.aigc.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chat History REST Controller
 * Manages conversation history in database
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;
    private final UserRepository userRepository;

    /**
     * Create a new conversation
     */
    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> createConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String title) {

        Long userId = getUserId(userDetails);
        ConversationResponse conversation = chatHistoryService.createConversation(userId, title);
        return ResponseEntity.ok(conversation);
    }

    /**
     * Get all conversations for current user
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationSummaryResponse>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        List<ConversationSummaryResponse> conversations = chatHistoryService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get specific conversation with messages
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<?> getConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String conversationId) {

        Long userId = getUserId(userDetails);
        return chatHistoryService.getConversation(conversationId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get messages for a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ConversationResponse.MessageResponse>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String conversationId) {

        Long userId = getUserId(userDetails);
        List<ConversationResponse.MessageResponse> messages = chatHistoryService.getMessages(conversationId, userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Save a message to conversation
     */
    @PostMapping("/messages")
    public ResponseEntity<Map<String, String>> saveMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChatHistoryRequest request) {

        Long userId = getUserId(userDetails);
        chatHistoryService.saveMessage(userId, request);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Message saved successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Update conversation title
     */
    @PutMapping("/conversations/{conversationId}/title")
    public ResponseEntity<Map<String, String>> updateConversationTitle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String conversationId,
            @RequestParam String title) {

        Long userId = getUserId(userDetails);
        chatHistoryService.updateConversationTitle(conversationId, userId, title);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Title updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Delete (soft delete) conversation
     */
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Map<String, String>> deleteConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String conversationId) {

        Long userId = getUserId(userDetails);
        chatHistoryService.deleteConversation(conversationId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Conversation deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Search conversations
     */
    @GetMapping("/conversations/search")
    public ResponseEntity<List<ConversationSummaryResponse>> searchConversations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword) {

        Long userId = getUserId(userDetails);
        List<ConversationSummaryResponse> results = chatHistoryService.searchConversations(userId, keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * Search messages
     */
    @GetMapping("/messages/search")
    public ResponseEntity<List<ConversationResponse.MessageResponse>> searchMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword) {

        Long userId = getUserId(userDetails);
        List<ConversationResponse.MessageResponse> results = chatHistoryService.searchMessages(userId, keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * Get conversation statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ChatHistoryService.ConversationStats> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        ChatHistoryService.ConversationStats stats = chatHistoryService.getConversationStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get user ID from authentication
     */
    private Long getUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
