package com.tiago_lima.social_media_app.controllers;

import com.tiago_lima.social_media_app.domain.entities.Conversation;
import com.tiago_lima.social_media_app.domain.entities.Message;
import com.tiago_lima.social_media_app.domain.entities.User;
import com.tiago_lima.social_media_app.repositories.UserRepository;
import com.tiago_lima.social_media_app.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final UserRepository userRepo;

    @PostMapping("/start/{targetId}")
    public ResponseEntity<?> startConversation(@PathVariable UUID targetId, Principal principal) {
        User me = userRepo.findByUsername(principal.getName()).orElseThrow();
        Conversation conv = messageService.startConversation(me.getId(), targetId);
        return ResponseEntity.ok(Map.of("id", conv.getId()));
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(Principal principal) {
        User me = userRepo.findByUsername(principal.getName()).orElseThrow();
        return ResponseEntity.ok(messageService.getUserConversations(me.getId()));
    }

    @PostMapping("/{conversationId}")
    public ResponseEntity<?> sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody Map<String, String> payload,
            Principal principal) {
        User me = userRepo.findByUsername(principal.getName()).orElseThrow();
        String content = payload.get("content");
        Message saved = messageService.sendMessage(conversationId, me.getId(), content);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getMessages(@PathVariable UUID conversationId) {
        return ResponseEntity.ok(messageService.getConversationMessages(conversationId));
    }

}
