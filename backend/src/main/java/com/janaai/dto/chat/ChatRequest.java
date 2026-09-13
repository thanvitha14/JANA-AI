package com.janaai.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    @NotBlank(message = "Message content is required")
    private String message;

    private String conversationId;

    private String language;
}
