package com.tiago_lima.social_media_app.controllers;

import com.tiago_lima.social_media_app.domain.dtos.LoginResponse;
import com.tiago_lima.social_media_app.domain.dtos.LoginResponseWithRefreshCookie;
import com.tiago_lima.social_media_app.domain.dtos.LoginUserDto;
import com.tiago_lima.social_media_app.domain.dtos.SignupResponse;
import com.tiago_lima.social_media_app.mappers.UserMapper;
import com.tiago_lima.social_media_app.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody LoginUserDto loginUserDto) {
        String username = authService.signup(userMapper.fromUserDto(loginUserDto)).getUsername();
        SignupResponse signupResponse = SignupResponse.builder()
                .username(username)
                .build();
        return new ResponseEntity<>(signupResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginUserDto loginUserDto) {
        LoginResponseWithRefreshCookie loginResponseWithRefreshCookie = authService.login(userMapper.fromUserDto(loginUserDto));
        boolean cookieUpdated = loginResponseWithRefreshCookie.getRefreshCookie() != null;
        LoginResponse loginResponse = LoginResponse.builder()
                .token(loginResponseWithRefreshCookie.getAccessToken())
                .expiresIn(loginResponseWithRefreshCookie.getAccessTokenExpiresIn())
                .cookieUpdated(cookieUpdated)
                .id(loginResponseWithRefreshCookie.getId())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, loginResponseWithRefreshCookie.getRefreshCookie().toString())
                .body(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@CookieValue("refreshToken") String refreshToken) {
        LoginResponseWithRefreshCookie loginResponseWithRefreshCookie = authService.refresh(refreshToken);
        boolean cookieUpdated = loginResponseWithRefreshCookie.getRefreshCookie() != null;
        LoginResponse loginResponse = LoginResponse.builder()
                .token(loginResponseWithRefreshCookie.getAccessToken())
                .expiresIn(loginResponseWithRefreshCookie.getAccessTokenExpiresIn())
                .cookieUpdated(cookieUpdated)
                .id(loginResponseWithRefreshCookie.getId())
                .build();

        if (cookieUpdated) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, loginResponseWithRefreshCookie.getRefreshCookie().toString())
                    .body(loginResponse);
        }

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        String deletedCookie = authService.deleteRefreshTokenCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deletedCookie)
                .build();
    }

}
