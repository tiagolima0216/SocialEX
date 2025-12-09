package com.tiago_lima.social_media_app.controllers;

import com.tiago_lima.social_media_app.domain.documents.UserSearchDocument;
import com.tiago_lima.social_media_app.services.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final UserSearchService userSearchService;

    @GetMapping("/users")
    public ResponseEntity<List<UserSearchDocument>> searchUsers(
            @RequestParam("q") String q,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        if (q == null || q.isBlank()) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(userSearchService.searchUsers(q, Math.min(size, 25)));
    }
}
