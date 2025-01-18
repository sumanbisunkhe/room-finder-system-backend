package com.roomfinder.service;

import com.roomfinder.dto.request.MessageRequest;
import com.roomfinder.entity.Message;

import java.util.List;

public interface MessageService {
    Message sendMessage(Long senderId, MessageRequest request);
    void markAsRead(Long messageId, Long userId);
    void deleteMessage(Long messageId, Long userId);
    List<Message> getRoomMessages(Long roomId, Long userId);
    List<Message> getConversation(Long otherUserId, Long currentUserId);
    List<Message> getUnreadMessages(Long userId);
    List<Long> getUserRooms(Long userId);
}