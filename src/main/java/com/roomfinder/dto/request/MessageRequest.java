package com.roomfinder.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {
    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "Content cannot be empty")
    @Size(min = 1, max = 2000, message = "Message content must be between 1 and 2000 characters")
    private String content;

    private Long roomId;
}