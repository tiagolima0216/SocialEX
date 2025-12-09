package com.tiago_lima.social_media_app.kafka.events;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent {
    public enum Type { CREATED, UPDATED, DELETED }

    private String id;
    private String username;
    private String displayName; // could be same as username if you don't have separate field
    private String bio;
    private String profilePicture; // stored path
    private Type type;
}
