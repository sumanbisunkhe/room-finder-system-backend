package com.roomfinder.controller;

import com.roomfinder.dto.request.MessageRequest;
import com.roomfinder.dto.response.ApiResponse;
import com.roomfinder.dto.response.DirectConversationResponse;
import com.roomfinder.dto.response.MessageResponse;
import com.roomfinder.entity.Message;
import com.roomfinder.service.MessageService;
import com.roomfinder.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SEEKER', 'LANDLORD')")
    public ResponseEntity<ApiResponse> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MessageRequest request) {
        try {
            var sender = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            MessageResponse response = messageService.sendAndNotifyMessage(sender.getId(), request);
            return ResponseEntity.ok(new ApiResponse(true, "Message sent successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{messageId}/read")
    @PreAuthorize("hasAnyRole('SEEKER', 'LANDLORD')")
    public ResponseEntity<ApiResponse> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long messageId) {
        try {
            var user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            messageService.markAsRead(messageId, user.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Message marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/room/{roomId}/read-all")
    @PreAuthorize("hasAnyRole('SEEKER', 'LANDLORD')")
    public ResponseEntity<ApiResponse> markRoomMessagesAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId) {
        try {
            var user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            messageService.markMessagesAsRead(roomId, user.getId());
            return ResponseEntity.ok(new ApiResponse(true, "All messages marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/direct-conversations")
    @PreAuthorize("hasAnyRole('SEEKER', 'LANDLORD')")
    public ResponseEntity<ApiResponse> getDirectConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<DirectConversationResponse> conversations = messageService.getDirectConversations(user.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Direct conversations retrieved successfully", conversations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasAnyRole('SEEKER', 'LANDLORD')")
    public ResponseEntity<ApiResponse> deleteMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long messageId) {
        try {
            var user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            messageService.deleteMessage(messageId, user.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Message deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('SEEKER', 'LANDLORD')")
    public ResponseEntity<ApiResponse> getRoomMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long roomId) {
        try {
            var user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Message> messages = messageService.getRoomMessages(roomId, user.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Messages retrieved successfully", messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/conversation/{otherUserId}")
    @PreAuthorize("hasAnyRole('SEEKER', 'LANDLORD')")
    public ResponseEntity<ApiResponse> getConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long otherUserId) {
        try {
            var user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Message> messages = messageService.getConversation(otherUserId, user.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Conversation retrieved successfully", messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('SEEKER', 'LANDLORD')")
    public ResponseEntity<ApiResponse> getUnreadMessages(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Message> messages = messageService.getUnreadMessages(user.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Unread messages retrieved successfully", messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('SEEKER', 'LANDLORD')")
    public ResponseEntity<ApiResponse> getRecentMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int count) {
        try {
            var user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<MessageResponse> messages = messageService.getRecentMessages(user.getId(), count);
            return ResponseEntity.ok(new ApiResponse(true, "Recent messages retrieved successfully", messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // WebSocket Message Handlers
    @MessageMapping("/send/message")
    public void handleWebSocketMessage(@Payload MessageRequest messageRequest,
                                       @Header("simpUser") Principal principal) {
        Long senderId = Long.parseLong(principal.getName());
        messageService.sendAndNotifyMessage(senderId, messageRequest);
    }

    @SubscribeMapping("/user/queue/messages")
    public void subscribeToUserMessages(Principal principal) {
        // Subscription confirmation if needed
    }

    @SubscribeMapping("/topic/room/{roomId}")
    public void subscribeToRoom(@DestinationVariable Long roomId, Principal principal) {
        // Subscription confirmation if needed
    }
}