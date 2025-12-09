package com.tiago_lima.social_media_app.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;

    @Getter
    private static final long ACCESS_TOKEN_EXPIRATION = 40 * 60; // 40min
    @Getter
    private static final long REFRESH_TOKEN_EXPIRATION = 30 * 24 * 60 * 60; // 30days
    @Getter
    private static final long REFRESH_INTERVAL_REFRESH_TOKEN_MILIS = 7L * 24 * 60 * 60 * 1000; //7days

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Convert the string secret to a proper SecretKey
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, final long expiresIn) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis() + expiresIn * 1000L))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean shouldRefreshEvery7Days(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date issuedAt = claims.getIssuedAt();
        long now = System.currentTimeMillis();

        // Issue new refresh token if 7 days have passed since original issuance
        return now - issuedAt.getTime() >= REFRESH_INTERVAL_REFRESH_TOKEN_MILIS;
    }

}