package com.roomfinder.service;

import com.roomfinder.dto.request.MessageRequest;
import com.roomfinder.dto.response.DirectConversationResponse;
import com.roomfinder.dto.response.MessageResponse;
import com.roomfinder.entity.Message;

import java.util.List;

public interface MessageService {
    Message sendMessage(Long senderId, MessageRequest request);
    MessageResponse sendAndNotifyMessage(Long senderId, MessageRequest request);
    void markAsRead(Long messageId, Long userId);
    void markMessagesAsRead(Long roomId, Long currentUserId);
    void deleteMessage(Long messageId, Long userId);
    List<Message> getRoomMessages(Long roomId, Long userId);
    List<Message> getConversation(Long otherUserId, Long currentUserId);
    List<Message> getUnreadMessages(Long userId);
    List<Long> getUserRooms(Long userId);
    List<DirectConversationResponse> getDirectConversations(Long userId);
    Message getMessageById(Long messageId);
    List<MessageResponse> getRecentMessages(Long userId, int count);
}