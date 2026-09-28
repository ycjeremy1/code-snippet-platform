package com.codeplatform.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Redis-backed JWT token management.
 * <p>
 * - Refresh tokens are "whitelisted": on login we store {@code refresh:{userId}:{jti} -> "1"}
 *   with a TTL equal to the refresh token's remaining lifetime. A refresh call is only honored
 *   if the presented token's jti is still present in Redis, which lets us revoke a single
 *   session (logout) or all sessions (change password) by deleting keys.
 * - Access tokens are "blacklisted" on logout: {@code blacklist:{jti} -> "1"} with a TTL equal
 *   to the token's remaining lifetime, so a stolen/leaked access token stops working immediately
 *   instead of waiting out its natural expiry.
 */
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void whitelistRefreshToken(Long userId, String jti, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative()) {
            return;
        }
        redisTemplate.opsForValue().set(refreshKey(userId, jti), "1", ttl);
    }

    public boolean isRefreshTokenValid(Long userId, String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(refreshKey(userId, jti)));
    }

    public void revokeRefreshToken(Long userId, String jti) {
        redisTemplate.delete(refreshKey(userId, jti));
    }

    /** Revoke every refresh token for a user (e.g. logout-all-devices, password change). */
    public void revokeAllRefreshTokens(Long userId) {
        var keys = redisTemplate.keys(REFRESH_PREFIX + userId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void blacklistAccessToken(String jti, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative()) {
            return;
        }
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "1", ttl);
    }

    public boolean isAccessTokenBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }

    private String refreshKey(Long userId, String jti) {
        return REFRESH_PREFIX + userId + ":" + jti;
    }
}
