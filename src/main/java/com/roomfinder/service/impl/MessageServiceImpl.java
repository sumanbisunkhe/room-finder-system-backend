package com.roomfinder.service.impl;

import com.roomfinder.dto.request.MessageRequest;
import com.roomfinder.dto.request.ValidateUsersRequest;
import com.roomfinder.entity.Message;
import com.roomfinder.exceptions.ResourceNotFoundException;
import com.roomfinder.exceptions.UnauthorizedAccessException;
import com.roomfinder.repository.MessageRepository;
import com.roomfinder.service.MessageService;
import com.roomfinder.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService; // Assume this exists for user validation

    @Override
    public Message sendMessage(Long senderId, MessageRequest request) {
        // Validate users exist and have appropriate roles
        validateUsers(senderId, request.getReceiverId());

        Message message = Message.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .roomId(request.getRoomId())
                .isRead(false)
                .build();

        return messageRepository.save(message);
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getReceiverId().equals(userId)) {
            throw new UnauthorizedAccessException("Not authorized to mark this message as read");
        }

        message.setRead(true);
        messageRepository.save(message);
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSenderId().equals(userId)) {
            throw new UnauthorizedAccessException("Not authorized to delete this message");
        }

        messageRepository.delete(message);
    }

    @Override
    public List<Message> getRoomMessages(Long roomId, Long userId) {
        // Verify user has access to this room
        validateRoomAccess(roomId, userId);
        return messageRepository.findByRoomIdOrderBySentAtDesc(roomId);
    }

    @Override
    public List<Message> getConversation(Long otherUserId, Long currentUserId) {
        validateUsers(currentUserId, otherUserId);
        return messageRepository.findBySenderIdAndReceiverIdOrderBySentAtDesc(
                currentUserId, otherUserId);
    }

    @Override
    public List<Message> getUnreadMessages(Long userId) {
        return messageRepository.findByReceiverIdAndIsReadFalse(userId);
    }

    @Override
    public List<Long> getUserRooms(Long userId) {
        return messageRepository.findAllRoomsByUserId(userId);
    }

    private void validateUsers(Long senderId, Long receiverId) {
        ValidateUsersRequest sender = userService.getUserById(senderId);
        ValidateUsersRequest receiver = userService.getUserById(receiverId);

        if (!sender.isValidRole() || !receiver.isValidRole()) {
            throw new UnauthorizedAccessException("Invalid user roles for messaging");
        }
    }

    private void validateRoomAccess(Long roomId, Long userId) {
        List<Long> userRooms = getUserRooms(userId);
        if (!userRooms.contains(roomId)) {
            throw new UnauthorizedAccessException("User does not have access to this room");
        }
    }
}