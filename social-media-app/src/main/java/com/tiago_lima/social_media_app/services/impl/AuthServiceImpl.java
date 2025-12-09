package com.tiago_lima.social_media_app.services.impl;

import com.tiago_lima.social_media_app.domain.dtos.LoginResponseWithRefreshCookie;
import com.tiago_lima.social_media_app.domain.entities.User;
import com.tiago_lima.social_media_app.kafka.events.UserEvent;
import com.tiago_lima.social_media_app.kafka.producers.UserEventProducer;
import com.tiago_lima.social_media_app.repositories.UserRepository;
import com.tiago_lima.social_media_app.security.UserDetailsServiceImpl;
import com.tiago_lima.social_media_app.services.AuthService;
import com.tiago_lima.social_media_app.utils.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserEventProducer userEventProducer;

    @Override
    public User signup(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalStateException("Username is already in use");
        }

        User newUser = User.builder()
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .role(user.getRole())
                .build();
        User savedUser = userRepository.save(newUser);

        userEventProducer.publish(UserEvent.builder()
                .id(savedUser.getId().toString())
                .username(savedUser.getUsername())
                //.displayName(user.getUsername()) // or a separate displayName
                .bio(savedUser.getBio())
                .profilePicture(savedUser.getProfilePicture())
                .type(UserEvent.Type.CREATED)
                .build());

        return savedUser;
    }

    @Override
    public LoginResponseWithRefreshCookie login(User user) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User savedUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String accessToken = jwtUtil.generateToken(userDetails.getUsername(), JwtUtil.getACCESS_TOKEN_EXPIRATION()); // 1h
        String refreshToken = jwtUtil.generateToken(userDetails.getUsername(), JwtUtil.getREFRESH_TOKEN_EXPIRATION()); // 30d

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofSeconds(JwtUtil.getREFRESH_TOKEN_EXPIRATION()))
                .sameSite("Lax")
                .build();

        User dataBaseUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        dataBaseUser.setLastLoginDate(LocalDateTime.now());
        userRepository.save(dataBaseUser);

        return LoginResponseWithRefreshCookie.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(JwtUtil.getACCESS_TOKEN_EXPIRATION())
                .refreshCookie(cookie)
                .id(savedUser.getId())
                .build();
    }

    @Override
    public LoginResponseWithRefreshCookie refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is empty");
        }
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));

        boolean issueNewRefreshToken = jwtUtil.shouldRefreshEvery7Days(refreshToken);

        String accessToken = jwtUtil.generateToken(userDetails.getUsername(), JwtUtil.getACCESS_TOKEN_EXPIRATION()); // 1h

        ResponseCookie cookie = null;
        String newRefreshToken;

        if (issueNewRefreshToken) {
            newRefreshToken = jwtUtil.generateToken(userDetails.getUsername(), JwtUtil.getREFRESH_TOKEN_EXPIRATION()); // 30d

            cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofSeconds(JwtUtil.getREFRESH_TOKEN_EXPIRATION()))
                    .sameSite("Lax")
                    .build();
        }

        return LoginResponseWithRefreshCookie.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(JwtUtil.getACCESS_TOKEN_EXPIRATION())
                .refreshCookie(cookie)
                .id(user.getId())
                .build();
    }

    @Override
    public String deleteRefreshTokenCookie() {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();
        return deleteCookie.toString();
    }



}
