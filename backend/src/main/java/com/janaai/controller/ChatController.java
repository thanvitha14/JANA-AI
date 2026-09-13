package com.janaai.controller;

import com.janaai.dto.chat.ChatConversationResponse;
import com.janaai.dto.chat.ChatRequest;
import com.janaai.dto.response.ApiResponse;
import com.janaai.entity.ChatMessage;
import com.janaai.entity.User;
import com.janaai.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChatRequest request
    ) {
        ChatMessage response = chatService.sendMessage(user, request.getMessage(), request.getConversationId(), request.getLanguage());
        return ResponseEntity.ok(ApiResponse.success(response, "Message processed successfully"));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ChatConversationResponse>>> getUserConversations(
            @AuthenticationPrincipal User user
    ) {
        List<ChatConversationResponse> response = chatService.getUserConversations(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Conversations retrieved successfully"));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getConversationHistory(
            @AuthenticationPrincipal User user,
            @PathVariable String conversationId
    ) {
        List<ChatMessage> response = chatService.getConversationHistory(user, conversationId);
        return ResponseEntity.ok(ApiResponse.success(response, "Conversation history retrieved successfully"));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @AuthenticationPrincipal User user,
            @PathVariable String conversationId
    ) {
        chatService.deleteConversation(user, conversationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Conversation deleted successfully"));
    }
}
