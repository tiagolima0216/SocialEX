package com.tiago_lima.social_media_app.kafka.consumers;

import com.tiago_lima.social_media_app.domain.documents.UserSearchDocument;
import com.tiago_lima.social_media_app.kafka.KafkaTopics;
import com.tiago_lima.social_media_app.kafka.events.UserEvent;
import com.tiago_lima.social_media_app.repositories.searchRepositories.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserSearchRepository repository;

    @KafkaListener(topics = KafkaTopics.USER_EVENTS, groupId = "user-indexer")
    public void onUserEvent(UserEvent event) {
        switch (event.getType()) {
            case DELETED -> repository.deleteById(event.getId());
            case CREATED, UPDATED -> {
                UserSearchDocument doc = UserSearchDocument.builder()
                        .id(event.getId())
                        .username(event.getUsername())
                        .displayName(event.getDisplayName())
                        .bio(event.getBio())
                        .profilePicture(event.getProfilePicture())
                        .build();
                repository.save(doc);
            }
        }
    }
}
