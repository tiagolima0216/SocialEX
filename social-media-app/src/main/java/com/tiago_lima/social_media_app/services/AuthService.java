package com.tiago_lima.social_media_app.services;

import com.tiago_lima.social_media_app.domain.dtos.LoginResponseWithRefreshCookie;
import com.tiago_lima.social_media_app.domain.entities.User;

public interface AuthService {
    User signup(User user);
    LoginResponseWithRefreshCookie login(User user);
    LoginResponseWithRefreshCookie refresh(String refreshToken);
    String deleteRefreshTokenCookie();
}
