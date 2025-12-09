package com.tiago_lima.social_media_app.controllers;

import com.tiago_lima.social_media_app.domain.entities.FriendRequest;
import com.tiago_lima.social_media_app.domain.entities.User;
import com.tiago_lima.social_media_app.repositories.FriendRequestRepository;
import com.tiago_lima.social_media_app.repositories.UserRepository;
import com.tiago_lima.social_media_app.services.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    @PostMapping("/{targetId}/request")
    public ResponseEntity<?> sendRequest(@PathVariable UUID targetId, Principal principal) {
        User requester = userRepository.findByUsername(principal.getName()).orElseThrow();
        friendService.sendFriendRequest(requester.getId(), targetId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{requestId}/respond")
    public ResponseEntity<?> respond(@PathVariable UUID requestId, @RequestParam boolean accept, Principal principal) {
        User actor = userRepository.findByUsername(principal.getName()).orElseThrow();
        friendService.respondToFriendRequest(requestId, actor.getId(), accept);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendRequest>> incomingRequests(Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<FriendRequest> list = friendRequestRepository.findByTargetAndStatus(user, FriendRequest.Status.PENDING);
        return ResponseEntity.ok(list);
    }
}
