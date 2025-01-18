package com.roomfinder.repository;

import com.roomfinder.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}