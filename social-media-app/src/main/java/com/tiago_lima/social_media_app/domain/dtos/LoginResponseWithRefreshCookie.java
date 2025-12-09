package com.tiago_lima.social_media_app.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseWithRefreshCookie {
    private UUID id;
    private String accessToken;
    private long accessTokenExpiresIn;
    private ResponseCookie refreshCookie;
}
