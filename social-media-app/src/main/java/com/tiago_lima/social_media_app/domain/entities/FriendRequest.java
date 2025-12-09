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
@Table(name = "friend_requests")
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_id")
    private User target;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    public enum Status { PENDING, ACCEPTED, REJECTED }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
