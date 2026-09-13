package com.janaai.repository;

import com.janaai.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository 
        extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findByConversationId(
            String conversationId
    );

    List<ChatMessage> findByUserId(
            String userId
    );
}