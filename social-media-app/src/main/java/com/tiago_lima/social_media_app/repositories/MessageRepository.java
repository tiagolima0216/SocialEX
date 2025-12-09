package com.tiago_lima.social_media_app.repositories;

import com.tiago_lima.social_media_app.domain.entities.Conversation;
import com.tiago_lima.social_media_app.domain.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
}
