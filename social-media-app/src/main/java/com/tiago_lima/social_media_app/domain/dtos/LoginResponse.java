package com.tiago_lima.social_media_app.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private UUID id;
    private String token;
    private long expiresIn;
    private boolean cookieUpdated;
}
