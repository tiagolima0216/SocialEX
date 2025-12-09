package com.tiago_lima.social_media_app.repositories;

import com.tiago_lima.social_media_app.domain.entities.Conversation;
import com.tiago_lima.social_media_app.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p = :user")
    List<Conversation> findByUser(@Param("user") User user);

    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 WHERE p1 = :u1 AND p2 = :u2")
    Optional<Conversation> findBetweenUsers(@Param("u1") User u1, @Param("u2") User u2);
}
