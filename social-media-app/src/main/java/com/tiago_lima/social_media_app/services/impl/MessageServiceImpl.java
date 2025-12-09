package com.tiago_lima.social_media_app.services.impl;

import com.tiago_lima.social_media_app.domain.entities.Conversation;
import com.tiago_lima.social_media_app.domain.entities.Message;
import com.tiago_lima.social_media_app.domain.entities.User;
import com.tiago_lima.social_media_app.repositories.ConversationRepository;
import com.tiago_lima.social_media_app.repositories.MessageRepository;
import com.tiago_lima.social_media_app.repositories.UserRepository;
import com.tiago_lima.social_media_app.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public Conversation startConversation(UUID currentUserId, UUID targetUserId) {
        User u1 = userRepo.findById(currentUserId).orElseThrow();
        User u2 = userRepo.findById(targetUserId).orElseThrow();

        Optional<Conversation> existing = conversationRepo.findBetweenUsers(u1, u2);

        return existing.orElseGet(() -> {
            Conversation newConv = new Conversation();
            newConv.getParticipants().add(u1);
            newConv.getParticipants().add(u2);
            return conversationRepo.save(newConv);
        });
    }

    public List<Conversation> getUserConversations(UUID userId) {
        User user = userRepo.findById(userId).orElseThrow();
        return conversationRepo.findByUser(user);
    }

    private final SimpUserRegistry simpUserRegistry;

    public void logUsers() {
        System.out.println("loggin users: ");
        simpUserRegistry.getUsers().forEach(u -> {
            System.out.println("Connected user: " + u.getName());
            u.getSessions().forEach(s ->
                    s.getSubscriptions().forEach(sub ->
                            System.out.println("Subscribed to: " + sub.getDestination())
                    )
            );
        });
    }

    public Message sendMessage(UUID conversationId, UUID senderId, String content) {
        Conversation conv = conversationRepo.findById(conversationId).orElseThrow();
        User sender = userRepo.findById(senderId).orElseThrow();

        Message msg = Message.builder()
                .conversation(conv)
                .sender(sender)
                .content(content)
                .readFlag(false)
                .build();

        Message saved = messageRepo.save(msg);

        Map<String, Object> payload = Map.of(
                "id", saved.getId(),
                "conversationId", conv.getId(),
                "senderId", sender.getId(),
                "senderUsername", sender.getUsername(),
                "content", saved.getContent(),
                "createdAt", saved.getCreatedAt().toInstant(ZoneOffset.UTC).toString(),
                "readFlag", saved.isReadFlag()
        );

        conv.getParticipants().stream()
                .filter(p -> !p.getId().equals(sender.getId()))
                .forEach(p -> messagingTemplate.convertAndSendToUser(
                        p.getUsername(),
                        "/queue/messages",
                        payload
                ));

        return saved;
    }


    public List<Message> getConversationMessages(UUID conversationId) {
        Conversation conv = conversationRepo.findById(conversationId).orElseThrow();
        return messageRepo.findByConversationOrderByCreatedAtAsc(conv);
    }
}
