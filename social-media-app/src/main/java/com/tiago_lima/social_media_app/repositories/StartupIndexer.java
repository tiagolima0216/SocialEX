package com.tiago_lima.social_media_app.repositories;

import com.tiago_lima.social_media_app.services.UserIndexerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupIndexer implements CommandLineRunner {

    private final UserRepository userRepository; // your JPA repo
    private final UserIndexerService userIndexerService;

    @Override
    public void run(String... args) {
        var users = userRepository.findAll();
        userIndexerService.indexAll(users);
    }
}