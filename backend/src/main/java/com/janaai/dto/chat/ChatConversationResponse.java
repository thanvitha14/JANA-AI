package com.janaai.dto.chat;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversationResponse {
    private String conversationId;
    private String title;
    private String lastMessagePreview;
    private LocalDateTime createdAt;
}
