package com.roomfinder.service;

import com.roomfinder.dto.request.MessageRequest;
import com.roomfinder.dto.request.ValidateUsersRequest;
import com.roomfinder.entity.Message;
import com.roomfinder.exceptions.ResourceNotFoundException;
import com.roomfinder.exceptions.UnauthorizedAccessException;
import com.roomfinder.repository.MessageRepository;
import com.roomfinder.service.UserService;
import com.roomfinder.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Message testMessage;
    private MessageRequest testRequest;
    private ValidateUsersRequest validSender;
    private ValidateUsersRequest validReceiver;

    @BeforeEach
    void setUp() {
        testMessage = Message.builder()
                .id(1L)
                .senderId(1L)
                .receiverId(2L)
                .content("Test message")
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .roomId(1L)
                .build();

        testRequest = MessageRequest.builder()
                .receiverId(2L)
                .content("Test message")
                .roomId(1L)
                .build();

        validSender = ValidateUsersRequest.builder()
                .userId(1L)
                .role("SEEKER")
                .build();

        validReceiver = ValidateUsersRequest.builder()
                .userId(2L)
                .role("LANDLORD")
                .build();
    }

    @Test
    void sendMessage_Success() {
        when(userService.getUserById(1L)).thenReturn(validSender);
        when(userService.getUserById(2L)).thenReturn(validReceiver);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        Message result = messageService.sendMessage(1L, testRequest);

        assertNotNull(result);
        assertEquals(testMessage.getContent(), result.getContent());
        assertEquals(testMessage.getSenderId(), result.getSenderId());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void sendMessage_InvalidRole_ThrowsException() {
        ValidateUsersRequest invalidUser = ValidateUsersRequest.builder()
                .userId(1L)
                .role("INVALID_ROLE")
                .build();

        when(userService.getUserById(1L)).thenReturn(invalidUser);
        when(userService.getUserById(2L)).thenReturn(validReceiver);

        assertThrows(UnauthorizedAccessException.class,
                () -> messageService.sendMessage(1L, testRequest));
    }

    @Test
    void markAsRead_Success() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        messageService.markAsRead(1L, 2L);

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void markAsRead_UnauthorizedUser_ThrowsException() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        assertThrows(UnauthorizedAccessException.class,
                () -> messageService.markAsRead(1L, 1L));
    }

    @Test
    void deleteMessage_Success() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        messageService.deleteMessage(1L, 1L);

        verify(messageRepository).delete(testMessage);
    }

    @Test
    void deleteMessage_UnauthorizedUser_ThrowsException() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        assertThrows(UnauthorizedAccessException.class,
                () -> messageService.deleteMessage(1L, 2L));
    }

    @Test
    void getRoomMessages_Success() {
        List<Message> messages = Arrays.asList(testMessage);
        when(messageRepository.findAllRoomsByUserId(1L))
                .thenReturn(Arrays.asList(1L));
        when(messageRepository.findByRoomIdOrderBySentAtDesc(1L))
                .thenReturn(messages);

        List<Message> result = messageService.getRoomMessages(1L, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(messages.size(), result.size());
    }

    @Test
    void getRoomMessages_UnauthorizedAccess_ThrowsException() {
        when(messageRepository.findAllRoomsByUserId(1L))
                .thenReturn(Arrays.asList(2L));

        assertThrows(UnauthorizedAccessException.class,
                () -> messageService.getRoomMessages(1L, 1L));
    }

    @Test
    void getConversation_Success() {
        List<Message> messages = Arrays.asList(testMessage);
        when(userService.getUserById(1L)).thenReturn(validSender);
        when(userService.getUserById(2L)).thenReturn(validReceiver);
        when(messageRepository.findBySenderIdAndReceiverIdOrderBySentAtDesc(1L, 2L))
                .thenReturn(messages);

        List<Message> result = messageService.getConversation(2L, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(messages.size(), result.size());
    }

    @Test
    void getUnreadMessages_Success() {
        List<Message> messages = Arrays.asList(testMessage);
        when(messageRepository.findByReceiverIdAndIsReadFalse(1L))
                .thenReturn(messages);

        List<Message> result = messageService.getUnreadMessages(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(messages.size(), result.size());
    }

    @Test
    void getUserRooms_Success() {
        List<Long> roomIds = Arrays.asList(1L, 2L);
        when(messageRepository.findAllRoomsByUserId(1L)).thenReturn(roomIds);

        List<Long> result = messageService.getUserRooms(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(roomIds.size(), result.size());
    }
}