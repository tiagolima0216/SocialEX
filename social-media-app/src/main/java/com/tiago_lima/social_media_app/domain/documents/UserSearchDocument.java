package com.tiago_lima.social_media_app.domain.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "users")
@Setting(settingPath = "/es/users-settings.json")
//@Mapping(mappingPath = "/es/users-mappings.json")
public class UserSearchDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String username;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String displayName;

    private String bio;

    private String profilePicture;
}
