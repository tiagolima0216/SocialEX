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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // stored as BCrypt hash

    @Builder.Default
    private String role = "ROLE_USER"; // basic role

    private LocalDateTime signupDate;

    private LocalDateTime lastLoginDate;

    private String profilePicture;

    private String bio;

    @PrePersist
    protected void onCreate() {
        signupDate = LocalDateTime.now();
    }
}