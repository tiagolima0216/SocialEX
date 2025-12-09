package com.tiago_lima.social_media_app.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiago_lima.social_media_app.domain.entities.NotificationEntity;
import com.tiago_lima.social_media_app.domain.entities.PushSubscription;
import com.tiago_lima.social_media_app.domain.entities.User;
import com.tiago_lima.social_media_app.repositories.NotificationRepository;
import com.tiago_lima.social_media_app.repositories.PushSubscriptionRepository;
import com.tiago_lima.social_media_app.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    // VAPID keys (store in application.properties or vault)
    private final String vapidPublicKey = "YOUR_VAPID_PUBLIC";
    private final String vapidPrivateKey = "YOUR_VAPID_PRIVATE";

    // Save + dispatch
    public void sendNotificationToUser(User recipient, String title, String body, Map<String,Object> data) {
        // 1) Save
        NotificationEntity n;
        try {
            n = NotificationEntity.builder()
                    .user(recipient)
                    .title(title)
                    .body(body)
                    .data(data == null ? null : new ObjectMapper().writeValueAsString(data))
                    .readFlag(false)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        notificationRepository.save(n);

        // 2) Send via WebSocket if online
        try {
            messagingTemplate.convertAndSendToUser(recipient.getUsername(), "/queue/notifications",
                    Map.of("title", title, "body", body, "data", data));
        } catch (Exception ex) {
            // ignore if WS not connected
        }

        // 3) Send Web Push if offline / subscription exists (best effort)
        List<PushSubscription> subs = pushSubscriptionRepository.findByUser(recipient);
        for (PushSubscription s : subs) {
            try {
                sendWebPush(s.getSubscriptionJson(), title, body, data);
            } catch (Exception e) {
                // log and optionally remove bad subscription
            }
        }
    }

    private void sendWebPush(String subscriptionJson, String title, String body, Map<String,Object> data) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(subscriptionJson);

        String endpoint = node.get("endpoint").asText();
        String p256dh = node.get("keys").get("p256dh").asText();
        String auth = node.get("keys").get("auth").asText();

        // Prepare payload
        Map<String, Object> payload = Map.of(
                "title", title,
                "body", body,
                "data", data
        );
        String payloadJson = mapper.writeValueAsString(payload);

        // Initialize PushService with your VAPID keys
        PushService pushService = new PushService();
        pushService.setPublicKey(vapidPublicKey);
        pushService.setPrivateKey(vapidPrivateKey);
        pushService.setSubject("mailto:admin@yourdomain.com");

        Notification notification = new Notification(endpoint, p256dh, auth, payloadJson);
        pushService.send(notification);
    }
}
