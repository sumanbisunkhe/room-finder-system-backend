package com.roomfinder.service;

import java.time.LocalDateTime;

public interface DirectConversationProjection {
    Long getOwnUserId();
    Long getOtherUserId();
    String getLastContent();
    LocalDateTime getLastSentAt();
    Integer getUnreadCount();
}