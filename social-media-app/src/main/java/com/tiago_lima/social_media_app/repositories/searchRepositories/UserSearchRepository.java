package com.tiago_lima.social_media_app.repositories.searchRepositories;

import com.tiago_lima.social_media_app.domain.documents.UserSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface UserSearchRepository extends ElasticsearchRepository<UserSearchDocument, String> {
    // For “contains” feel, we'll use multi_match in service (query builder) instead of derived methods.
    List<UserSearchDocument> findTop10ByDisplayNameContainingIgnoreCase(String q);
    List<UserSearchDocument> findTop10ByUsernameContainingIgnoreCase(String q);
}
