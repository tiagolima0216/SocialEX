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
@Table(name = "friendships", uniqueConstraints = @UniqueConstraint(columnNames = {"user_a_id", "user_b_id"}))
public class Friendship {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_a_id")
    private User userA;

    @ManyToOne
    @JoinColumn(name = "user_b_id")
    private User userB;

    private LocalDateTime since;

    @PrePersist
    public void onCreate() { since = LocalDateTime.now(); }
}
