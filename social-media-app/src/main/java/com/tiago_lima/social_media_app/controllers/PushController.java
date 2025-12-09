package com.tiago_lima.social_media_app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiago_lima.social_media_app.domain.entities.PushSubscription;
import com.tiago_lima.social_media_app.domain.entities.User;
import com.tiago_lima.social_media_app.repositories.PushSubscriptionRepository;
import com.tiago_lima.social_media_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushController {
    private final PushSubscriptionRepository repo;
    private final UserRepository userRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String,Object> subscriptionJson, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        PushSubscription ps;
        try {
            ps = PushSubscription.builder()
                    .user(user)
                    .subscriptionJson(new ObjectMapper().writeValueAsString(subscriptionJson))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        repo.save(ps);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String,Object> subscriptionJson, Principal principal) throws Exception {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.valueToTree(subscriptionJson);
        String endpoint = node.get("endpoint").asText();

        List<PushSubscription> subs = repo.findByUser(user);
        for (PushSubscription ps : subs) {
            JsonNode stored = mapper.readTree(ps.getSubscriptionJson());
            if (stored.get("endpoint").asText().equals(endpoint)) {
                repo.delete(ps);
            }
        }

        return ResponseEntity.ok(Map.of("status", "unsubscribed"));
    }

}
