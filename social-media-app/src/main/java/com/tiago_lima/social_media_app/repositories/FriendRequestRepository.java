package com.tiago_lima.social_media_app.repositories;

import com.tiago_lima.social_media_app.domain.entities.FriendRequest;
import com.tiago_lima.social_media_app.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    Optional<FriendRequest> findByRequesterAndTarget(User requester, User target);
    List<FriendRequest> findByTargetAndStatus(User target, FriendRequest.Status status);
}
