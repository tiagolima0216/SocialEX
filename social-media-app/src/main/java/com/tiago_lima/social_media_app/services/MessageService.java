package com.tiago_lima.social_media_app.services;

import com.tiago_lima.social_media_app.domain.entities.Conversation;
import com.tiago_lima.social_media_app.domain.entities.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Conversation startConversation(UUID currentUserId, UUID targetUserId);
    List<Conversation> getUserConversations(UUID userId);
    Message sendMessage(UUID conversationId, UUID senderId, String content);
    List<Message> getConversationMessages(UUID conversationId);
}
