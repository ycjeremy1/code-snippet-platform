package com.codeplatform.security;

import com.codeplatform.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates JWT access/refresh tokens. Each token carries a unique
 * "jti" claim so individual tokens can be tracked (whitelisted/blacklisted) in Redis
 * by {@link com.codeplatform.security.RedisTokenService}.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public GeneratedToken generateAccessToken(Long userId, String username) {
        return generate(userId, username, jwtProperties.getAccessTokenExpirationMs(), "access");
    }

    public GeneratedToken generateRefreshToken(Long userId, String username) {
        return generate(userId, username, jwtProperties.getRefreshTokenExpirationMs(), "refresh");
    }

    private GeneratedToken generate(Long userId, String username, long ttlMs, String type) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMs);

        String token = Jwts.builder()
                .id(jti)
                .subject(username)
                .claim("uid", userId)
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key())
                .compact();

        return new GeneratedToken(token, jti, expiry);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public record GeneratedToken(String token, String jti, Date expiresAt) {
    }
}
