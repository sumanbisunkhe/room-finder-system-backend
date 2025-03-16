//package com.roomfinder.config;
//
//import com.roomfinder.controller.AuthController;
//import com.roomfinder.entity.User;
//import com.roomfinder.exceptions.ResourceNotFoundException;
//import com.roomfinder.service.UserService;
//import lombok.Getter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.event.EventListener;
//import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.messaging.SessionConnectedEvent;
//import org.springframework.web.socket.messaging.SessionDisconnectEvent;
//
//import java.security.Principal;
//import java.util.Objects;
//
//@Component
//public class WebSocketEventListener {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final UserService userService;
//    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
//
//
//    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, UserService userService) {
//        this.messagingTemplate = messagingTemplate;
//        this.userService = userService;
//    }
//
//    @EventListener
//    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
//        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
//        String username = headers.getUser().getName();
//
//        User user = userService.getUserByUsername(username)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
//
//        messagingTemplate.convertAndSend(
//                "/topic/userOnline",
//                new UserStatusEvent(user.getId(), true)
//        );
//    }
//
//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
//        Principal user = headers.getUser();
//
//        if (user == null) {
//            log.warn("WebSocket disconnected without authentication");
//            return;
//        }
//
//        String username = user.getName();
//        User userEntity = userService.getUserByUsername(username)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
//
//        messagingTemplate.convertAndSend(
//                "/topic/userOffline",
//                new UserStatusEvent(userEntity.getId(), false)
//        );
//    }
//
//    @Getter
//    public static class UserStatusEvent {
//        private final Long userId;
//        private final boolean online;
//
//        public UserStatusEvent(Long userId, boolean online) {
//            this.userId = userId;
//            this.online = online;
//        }
//    }
//}