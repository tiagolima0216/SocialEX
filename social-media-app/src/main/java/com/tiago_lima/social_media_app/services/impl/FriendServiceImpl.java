package com.tiago_lima.social_media_app.services.impl;

import com.tiago_lima.social_media_app.domain.entities.FriendRequest;
import com.tiago_lima.social_media_app.domain.entities.Friendship;
import com.tiago_lima.social_media_app.domain.entities.User;
import com.tiago_lima.social_media_app.repositories.FriendRequestRepository;
import com.tiago_lima.social_media_app.repositories.FriendshipRepository;
import com.tiago_lima.social_media_app.repositories.UserRepository;
import com.tiago_lima.social_media_app.services.FriendService;
import com.tiago_lima.social_media_app.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {
    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final NotificationService notificationService;

    public FriendRequest sendFriendRequest(UUID requesterId, UUID targetId) {
        User requester = userRepository.findById(requesterId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();

        // prevent duplicates & self requests
        if (requesterId.equals(targetId)) throw new IllegalArgumentException("Cannot friend yourself");

        friendRequestRepository.findByRequesterAndTarget(requester, target)
                .ifPresent(fr -> { throw new IllegalStateException("Already requested"); });

        FriendRequest fr = FriendRequest.builder()
                .requester(requester)
                .target(target)
                .status(FriendRequest.Status.PENDING)
                .build();
        fr = friendRequestRepository.save(fr);

        // notify target
        notificationService.sendNotificationToUser(target,
                "New friend request",
                requester.getUsername() + " sent you a friend request",
                Map.of("type", "friend_request", "requestId", fr.getId().toString(), "fromId", requesterId.toString()));

        return fr;
    }

    public void respondToFriendRequest(UUID requestId, UUID actorId, boolean accept) {
        FriendRequest fr = friendRequestRepository.findById(requestId).orElseThrow();
        if (!fr.getTarget().getId().equals(actorId)) throw new AccessDeniedException("Not allowed");

        fr.setStatus(accept ? FriendRequest.Status.ACCEPTED : FriendRequest.Status.REJECTED);
        fr.setRespondedAt(LocalDateTime.now());
        friendRequestRepository.save(fr);

        // if accepted, create friendship (store canonical order to avoid duplicates)
        if (accept) {
            Friendship f = Friendship.builder()
                    .userA(fr.getRequester())
                    .userB(fr.getTarget())
                    .build();
            friendshipRepository.save(f);

            // create conversation for messaging (optional)
            // notify requester
            notificationService.sendNotificationToUser(fr.getRequester(),
                    fr.getTarget().getUsername() + " accepted your friend request",
                    "",
                    Map.of("type", "friend_request_accepted", "friendId", fr.getTarget().getId().toString()));
        } else {
            notificationService.sendNotificationToUser(fr.getRequester(),
                    fr.getTarget().getUsername() + " rejected your friend request",
                    "",
                    Map.of("type", "friend_request_rejected"));
        }
    }
}
