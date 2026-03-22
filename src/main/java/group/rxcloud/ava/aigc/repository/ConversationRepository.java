package group.rxcloud.ava.aigc.repository;

import group.rxcloud.ava.aigc.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Conversation entity
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByConversationId(String conversationId);

    List<Conversation> findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(Long userId);

    Page<Conversation> findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    Optional<Conversation> findByConversationIdAndUserId(String conversationId, Long userId);

    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId AND c.isActive = true " +
           "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR EXISTS (SELECT m FROM c.messages m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :search, '%'))))")
    List<Conversation> searchByUserIdAndKeyword(@Param("userId") Long userId, @Param("search") String search);

    long countByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.user.id = :userId AND c.isActive = true AND c.updatedAt >= :since")
    long countRecentConversations(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
