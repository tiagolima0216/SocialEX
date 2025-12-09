package com.tiago_lima.social_media_app.services;

import com.tiago_lima.social_media_app.domain.entities.User;

import java.util.Map;

public interface NotificationService {
    void sendNotificationToUser(User recipient, String title, String body, Map<String,Object> data);
}
