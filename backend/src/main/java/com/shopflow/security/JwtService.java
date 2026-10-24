package com.shopflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${shopflow.jwt.secret}") String secret,
            @Value("${shopflow.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${shopflow.jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(UserPrincipal user) {
        return buildToken(user.getId(), user.getEmail(), user.getRole().name(), accessExpirationMs);
    }

    public String generateRefreshToken(UserPrincipal user) {
        return buildToken(user.getId(), user.getEmail(), user.getRole().name(), refreshExpirationMs);
    }

    private String buildToken(Long userId, String email, String role, long ttlMs) {
        var now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMs))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parse(token).getSubject());
    }
}
