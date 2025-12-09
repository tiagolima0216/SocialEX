package com.tiago_lima.social_media_app.services;

import com.tiago_lima.social_media_app.domain.entities.User;

import java.util.List;

public interface UserIndexerService {
    void indexUser(User user);
    void indexAll(List<User> users);
}
