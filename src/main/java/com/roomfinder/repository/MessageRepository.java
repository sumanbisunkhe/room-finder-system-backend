package com.roomfinder.repository;

import com.roomfinder.entity.Message;
import com.roomfinder.service.DirectConversationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomIdOrderBySentAtDesc(Long roomId);
    List<Message> findBySenderIdAndReceiverIdOrderBySentAtDesc(Long senderId, Long receiverId);
    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);

    @Query("SELECT DISTINCT m.roomId FROM Message m " +
            "WHERE m.senderId = :userId OR m.receiverId = :userId " +
            "AND m.roomId IS NOT NULL")
    List<Long> findAllRoomsByUserId(Long userId);

    @Query("SELECT m FROM Message m " +
            "WHERE (m.senderId = :user1 AND m.receiverId = :user2) " +
            "OR (m.senderId = :user2 AND m.receiverId = :user1) " +
            "ORDER BY m.sentAt DESC")
    List<Message> findConversationBetweenUsers(@Param("user1") Long user1,
                                               @Param("user2") Long user2);

    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END FROM Message m " +
            "WHERE (m.senderId = :userId OR m.receiverId = :userId)")
    List<Long> findDistinctDirectConversationUserIds(@Param("userId") Long userId);

    @Query(value =
            "WITH ranked_messages AS (" +
                    "   SELECT " +
                    "       CASE WHEN m.sender_id = :userId THEN m.receiver_id ELSE m.sender_id END as otherUserId, " +
                    "       m.content as lastContent, " +
                    "       m.sent_at as lastSentAt, " +
                    "       ROW_NUMBER() OVER (PARTITION BY CASE WHEN m.sender_id = :userId THEN m.receiver_id ELSE m.sender_id END ORDER BY m.sent_at DESC) as rn " +
                    "   FROM messages m " +
                    "   WHERE (m.sender_id = :userId OR m.receiver_id = :userId)" +
                    "), " +
                    "unread_counts AS (" +
                    "   SELECT " +
                    "       m.sender_id as otherUserId, " +
                    "       COUNT(*) as unreadCount " +
                    "   FROM messages m " +
                    "   WHERE m.receiver_id = :userId AND m.is_read = false " +
                    "   GROUP BY m.sender_id" +
                    ") " +
                    "SELECT " +
                    "   CAST(:userId AS BIGINT) as ownUserId, " +
                    "   rm.otherUserId as otherUserId, " +
                    "   rm.lastContent as lastContent, " +
                    "   rm.lastSentAt as lastSentAt, " +
                    "   COALESCE(uc.unreadCount, 0) as unreadCount " +
                    "FROM ranked_messages rm " +
                    "LEFT JOIN unread_counts uc ON rm.otherUserId = uc.otherUserId " +
                    "WHERE rm.rn = 1", nativeQuery = true)
    List<DirectConversationProjection> findDirectConversationsWithDetails(@Param("userId") Long userId);
}