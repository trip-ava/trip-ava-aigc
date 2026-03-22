package group.rxcloud.ava.aigc.repository;

import group.rxcloud.ava.aigc.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Message entity
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationConversationIdOrderByTimestampAsc(String conversationId);

    Page<Message> findByConversationConversationIdOrderByTimestampAsc(String conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversation.user.id = :userId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Message> searchByUserIdAndContentContaining(@Param("userId") Long userId, @Param("keyword") String keyword);

    long countByConversationConversationId(String conversationId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.user.id = :userId AND m.timestamp >= :since")
    long countRecentMessages(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId ORDER BY m.timestamp DESC")
    List<Message> findRecentMessagesByConversationId(@Param("conversationId") String conversationId, Pageable pageable);
}
