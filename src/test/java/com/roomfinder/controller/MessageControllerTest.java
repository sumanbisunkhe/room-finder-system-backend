package com.roomfinder.controller;

import com.roomfinder.dto.request.MessageRequest;
import com.roomfinder.dto.response.ApiResponse;
import com.roomfinder.entity.Message;
import com.roomfinder.entity.User;
import com.roomfinder.service.MessageService;
import com.roomfinder.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private MessageController messageController;

    private User testUser;
    private Message testMessage;
    private MessageRequest testMessageRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        testMessage = Message.builder()
                .id(1L)
                .senderId(1L)
                .receiverId(2L)
                .content("Test message")
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .roomId(1L)
                .build();

        testMessageRequest = MessageRequest.builder()
                .receiverId(2L)
                .content("Test message")
                .roomId(1L)
                .build();

        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void sendMessage_Success() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(messageService.sendMessage(eq(1L), any(MessageRequest.class))).thenReturn(testMessage);

        ResponseEntity<ApiResponse> response = messageController.sendMessage(userDetails, testMessageRequest);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Message sent successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        verify(messageService).sendMessage(eq(1L), any(MessageRequest.class));
    }

    @Test
    void sendMessage_UserNotFound() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse> response = messageController.sendMessage(userDetails, testMessageRequest);

        assertTrue(response.getStatusCode().is4xxClientError());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found", response.getBody().getMessage());
        verify(messageService, never()).sendMessage(any(), any());
    }

    @Test
    void markAsRead_Success() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(messageService).markAsRead(1L, 1L);

        ResponseEntity<ApiResponse> response = messageController.markAsRead(userDetails, 1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Message marked as read", response.getBody().getMessage());
        verify(messageService).markAsRead(1L, 1L);
    }

    @Test
    void deleteMessage_Success() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(messageService).deleteMessage(1L, 1L);

        ResponseEntity<ApiResponse> response = messageController.deleteMessage(userDetails, 1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Message deleted successfully", response.getBody().getMessage());
        verify(messageService).deleteMessage(1L, 1L);
    }

    @Test
    void getRoomMessages_Success() {
        List<Message> messages = Arrays.asList(testMessage);
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(messageService.getRoomMessages(1L, 1L)).thenReturn(messages);

        ResponseEntity<ApiResponse> response = messageController.getRoomMessages(userDetails, 1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Messages retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        verify(messageService).getRoomMessages(1L, 1L);
    }

    @Test
    void getConversation_Success() {
        List<Message> messages = Arrays.asList(testMessage);
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(messageService.getConversation(2L, 1L)).thenReturn(messages);

        ResponseEntity<ApiResponse> response = messageController.getConversation(userDetails, 2L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Conversation retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        verify(messageService).getConversation(2L, 1L);
    }

    @Test
    void getUnreadMessages_Success() {
        List<Message> messages = Arrays.asList(testMessage);
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(messageService.getUnreadMessages(1L)).thenReturn(messages);

        ResponseEntity<ApiResponse> response = messageController.getUnreadMessages(userDetails);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Unread messages retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        verify(messageService).getUnreadMessages(1L);
    }

    @Test
    void handleException_ServiceThrowsException() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(messageService.getUnreadMessages(1L)).thenThrow(new RuntimeException("Test error"));

        ResponseEntity<ApiResponse> response = messageController.getUnreadMessages(userDetails);

        assertTrue(response.getStatusCode().is4xxClientError());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Test error", response.getBody().getMessage());
    }
}