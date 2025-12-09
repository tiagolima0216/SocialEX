package com.tiago_lima.social_media_app.repositories;

import com.tiago_lima.social_media_app.domain.entities.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
}
