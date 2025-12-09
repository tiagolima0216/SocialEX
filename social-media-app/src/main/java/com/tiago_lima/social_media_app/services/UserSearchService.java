package com.tiago_lima.social_media_app.services;

import com.tiago_lima.social_media_app.domain.documents.UserSearchDocument;

import java.util.List;

public interface UserSearchService {
    List<UserSearchDocument> searchUsers(String q, int size);
}
