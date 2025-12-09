package com.tiago_lima.social_media_app.services;

import com.tiago_lima.social_media_app.domain.entities.FriendRequest;

import java.util.UUID;

public interface FriendService {
    FriendRequest sendFriendRequest(UUID requesterId, UUID targetId);
    void respondToFriendRequest(UUID requestId, UUID actorId, boolean accept);
}
