package com.tiago_lima.social_media_app.services.impl;

import com.tiago_lima.social_media_app.domain.documents.UserSearchDocument;
import com.tiago_lima.social_media_app.domain.entities.User;
import com.tiago_lima.social_media_app.repositories.searchRepositories.UserSearchRepository;
import com.tiago_lima.social_media_app.services.UserIndexerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserIndexerServiceImpl implements UserIndexerService {

    private final UserSearchRepository userSearchRepository;

    public void indexUser(User user) {
        UserSearchDocument doc = UserSearchDocument.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                //.displayName(user.getDisplayName())
                .profilePicture(user.getProfilePicture()) // adjust field names
                .build();

        userSearchRepository.save(doc);
    }

    public void indexAll(List<User> users) {
        List<UserSearchDocument> docs = users.stream().map(user -> UserSearchDocument.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                //.displayName(user.getDisplayName())
                .profilePicture(user.getProfilePicture())
                .build()).toList();

        userSearchRepository.saveAll(docs);
    }
}