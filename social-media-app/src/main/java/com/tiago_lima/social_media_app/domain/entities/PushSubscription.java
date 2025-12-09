package com.tiago_lima.social_media_app.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "push_subscriptions")
public class PushSubscription {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    private String subscriptionJson; // store the full JSON payload from browser

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() { createdAt = LocalDateTime.now(); }
}
