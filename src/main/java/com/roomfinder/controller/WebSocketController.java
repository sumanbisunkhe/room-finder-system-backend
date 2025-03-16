//package com.roomfinder.controller;
//
//import com.roomfinder.dto.request.MessageRequest;
//import com.roomfinder.entity.Message;
//import com.roomfinder.entity.User;
//import com.roomfinder.exceptions.ErrorResponse;
//import com.roomfinder.exceptions.ResourceNotFoundException;
//import com.roomfinder.service.MessageService;
//import com.roomfinder.service.UserService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.messaging.simp.annotation.SendToUser;
//import org.springframework.stereotype.Controller;
//
//import java.security.Principal;
//
//@Slf4j
//@Controller
//public class WebSocketController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final MessageService messageService;
//    private final UserService userService;
//
//    public WebSocketController(SimpMessagingTemplate messagingTemplate,
//                               MessageService messageService,
//                               UserService userService) {
//        this.messagingTemplate = messagingTemplate;
//        this.messageService = messageService;
//        this.userService = userService;
//    }
//
//    @MessageMapping("/chat.send")
//    public void handleMessage(@Payload MessageRequest messageRequest,
//                              Principal principal) {
//        log.debug("Received message from {}: {}", principal.getName(), messageRequest);
//
//        // Validate message
//        if (messageRequest.getContent() == null || messageRequest.getContent().trim().isEmpty()) {
//            throw new IllegalArgumentException("Message content cannot be empty");
//        }
//
//        if (messageRequest.getRoomId() == null && messageRequest.getReceiverId() == null) {
//            throw new IllegalArgumentException("Either roomId or receiverId must be specified");
//        }
//
//        // Get sender
//        User sender = userService.getUserByUsername(principal.getName())
//                .orElseThrow(() -> new ResourceNotFoundException("Sender not found: " + principal.getName()));
//
//        try {
//            Message savedMessage = messageService.sendMessage(sender.getId(), messageRequest);
//            deliverMessage(savedMessage, sender);
//
//            log.debug("Message successfully processed and delivered. MessageId: {}", savedMessage.getId());
//        } catch (Exception e) {
//            log.error("Error processing message: {}", e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    private void deliverMessage(Message message, User sender) {
//        if (message.getRoomId() != null) {
//            // Room message
//            String destination = "/topic/room." + message.getRoomId();
//            log.debug("Broadcasting room message to: {}", destination);
//            messagingTemplate.convertAndSend(destination, message);
//        } else {
//            // Direct message
//            User receiver = userService.loadUserById(message.getReceiverId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));
//
//            // Send to sender's queue
//            String senderDestination = "/queue/messages";
//            log.debug("Sending message to sender {}: {}", sender.getUsername(), senderDestination);
//            messagingTemplate.convertAndSendToUser(sender.getUsername(), senderDestination, message);
//
//            // Send to receiver's queue
//            String receiverDestination = "/queue/messages";
//            log.debug("Sending message to receiver {}: {}", receiver.getUsername(), receiverDestination);
//            messagingTemplate.convertAndSendToUser(receiver.getUsername(), receiverDestination, message);
//        }
//    }
//
//    @MessageMapping("/chat.typing")
//    @SendTo("/topic/typing")
//    public String handleTypingEvent(@Payload String username) {
//        return username + " is typing...";
//    }
//
////    @MessageExceptionHandler
////    public void handleException(Exception ex, Principal principal) {
////        log.error("WebSocket error for user {}: {}", principal.getName(), ex.getMessage(), ex);
////
////        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
////
////        messagingTemplate.convertAndSendToUser(
////                principal.getName(),
////                "/queue/errors",
////                errorResponse
////        );
////    }
//
//    @MessageExceptionHandler
//    @SendToUser("/queue/errors")
//    public ErrorResponse handleException(Exception ex) {
//        log.error("WebSocket message handling error: {}", ex.getMessage());
//        return new ErrorResponse(ex.getMessage());
//    }
//}