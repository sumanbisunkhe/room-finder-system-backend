package com.roomfinder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DirectConversationResponse {
    private Long ownUserId;
    private Long otherUserId;
    private LastMessage lastMessage;
    private int unreadCount;

    @Data
    @AllArgsConstructor
    public static class LastMessage {
        private String content;
        private LocalDateTime sentAt;
    }
}