package com.tiago_lima.social_media_app.services.impl;

import com.tiago_lima.social_media_app.domain.documents.UserSearchDocument;
import com.tiago_lima.social_media_app.services.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;

import java.util.List;

import static co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.wildcard;

@Service
@RequiredArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

    private final ElasticsearchOperations esOps;

    public List<UserSearchDocument> searchUsers(String q, int size) {
        if (q == null || q.isBlank()) return List.of();

        // Add wildcard on both fields
        var query = NativeQuery.builder()
                .withQuery(wildcard(w -> w
                        .field("username")
                        .value("*" + q.toLowerCase() + "*") // substring match
                ))
                .withPageable(PageRequest.of(0, size))
                .build();

        SearchHits<UserSearchDocument> hits = esOps.search(query, UserSearchDocument.class);
        return hits.stream().map(SearchHit::getContent).toList();
    }
}
