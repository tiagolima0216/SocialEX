package com.tiago_lima.social_media_app.kafka.producers;

import com.tiago_lima.social_media_app.kafka.KafkaTopics;
import com.tiago_lima.social_media_app.kafka.events.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventProducer {
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void publish(UserEvent event) {
        kafkaTemplate.send(KafkaTopics.USER_EVENTS, event.getId(), event);
    }
}
