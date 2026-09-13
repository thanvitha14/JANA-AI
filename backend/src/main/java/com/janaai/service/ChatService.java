package com.janaai.service;

import com.janaai.entity.ChatMessage;
import com.janaai.entity.User;
import com.janaai.dto.chat.ChatConversationResponse;

import java.util.List;

public interface ChatService {

    ChatMessage sendMessage(
            User user,
            String message,
            String conversationId,
            String language
    );


    List<ChatConversationResponse> getUserConversations(
            User user
    );


    List<ChatMessage> getConversationHistory(
            User user,
            String conversationId
    );


    void deleteConversation(
            User user,
            String conversationId
    );

}