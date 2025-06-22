package com.roomfinder.service.impl;

import com.roomfinder.dto.request.MessageRequest;
import com.roomfinder.dto.request.ValidateUsersRequest;
import com.roomfinder.dto.response.DirectConversationResponse;
import com.roomfinder.dto.response.MessageResponse;
import com.roomfinder.entity.Message;
import com.roomfinder.exceptions.ResourceNotFoundException;
import com.roomfinder.exceptions.UnauthorizedAccessException;
import com.roomfinder.repository.MessageRepository;
import com.roomfinder.service.DirectConversationProjection;
import com.roomfinder.service.MessageService;
import com.roomfinder.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Message sendMessage(Long senderId, MessageRequest request) {
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
    public MessageResponse sendAndNotifyMessage(Long senderId, MessageRequest request) {
        Message message = sendMessage(senderId, request);
        MessageResponse response = convertToResponse(message);
        notifyRecipients(response);
        return response;
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

        // Notify sender that message was read
        if (message.getRoomId() == null) { // Only for direct messages
            MessageResponse response = convertToResponse(message);
            messagingTemplate.convertAndSendToUser(
                    message.getSenderId().toString(),
                    "/queue/messages/read",
                    response
            );
        }
    }

    @Override
    public void markMessagesAsRead(Long roomId, Long currentUserId) {
        List<Message> unreadMessages = messageRepository.findByRoomIdAndReceiverIdAndIsReadFalse(roomId, currentUserId);
        unreadMessages.forEach(message -> message.setRead(true));
        messageRepository.saveAll(unreadMessages);
    }



    @Override
    public List<DirectConversationResponse> getDirectConversations(Long userId) {
        List<DirectConversationProjection> projections = messageRepository.findDirectConversationsWithDetails(userId);
        return projections.stream()
                .map(proj -> new DirectConversationResponse(
                        proj.getOwnUserId(),
                        proj.getOtherUserId(),
                        new DirectConversationResponse.LastMessage(
                                proj.getLastContent(),
                                proj.getLastSentAt()),
                        proj.getUnreadCount() != null ? proj.getUnreadCount() : 0))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSenderId().equals(userId)) {
            throw new UnauthorizedAccessException("Not authorized to delete this message");
        }

        messageRepository.delete(message);

        // Notify recipient about message deletion
        MessageResponse response = convertToResponse(message);
        response.setContent("This message was deleted");
        response.setDeleted(true);

        notifyRecipients(response);
    }

    @Override
    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
    }

    @Override
    public List<Message> getRoomMessages(Long roomId, Long userId) {
        validateRoomAccess(roomId, userId);
        return messageRepository.findByRoomIdOrderBySentAtDesc(roomId);
    }

    @Override
    public List<Message> getConversation(Long otherUserId, Long currentUserId) {
        validateUsers(currentUserId, otherUserId);
        return messageRepository.findConversationBetweenUsers(currentUserId, otherUserId);
    }

    @Override
    public List<Message> getUnreadMessages(Long userId) {
        return messageRepository.findByReceiverIdAndIsReadFalse(userId);
    }

    @Override
    public List<Long> getUserRooms(Long userId) {
        return messageRepository.findAllRoomsByUserId(userId);
    }

    @Override
    public List<MessageResponse> getRecentMessages(Long userId, int count) {
        Pageable pageable = PageRequest.of(0, count);
        return messageRepository.findByReceiverIdOrSenderIdOrderBySentAtDesc(userId, userId, pageable)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    private MessageResponse convertToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .isRead(message.isRead())
                .roomId(message.getRoomId())
                .deleted(false)
                .build();
    }

    private void notifyRecipients(MessageResponse response) {
        if (response.getRoomId() == null) {
            // Direct message - notify recipient
            messagingTemplate.convertAndSendToUser(
                    response.getReceiverId().toString(),
                    "/queue/messages",
                    response
            );
        } else {
            // Room message - notify all room subscribers
            messagingTemplate.convertAndSend(
                    "/topic/room/" + response.getRoomId(),
                    response
            );
        }

        // Also send back to sender for UI sync
        messagingTemplate.convertAndSendToUser(
                response.getSenderId().toString(),
                "/queue/messages",
                response
        );
    }

    private void validateUsers(Long senderId, Long receiverId) {
        ValidateUsersRequest sender = userService.getUserById(senderId);
        ValidateUsersRequest receiver = userService.getUserById(receiverId);

        if (sender.isValidRole() || receiver.isValidRole()) {
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