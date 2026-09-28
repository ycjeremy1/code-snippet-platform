package com.codeplatform.security;

import com.codeplatform.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("unit-test-secret-key-must-be-at-least-256-bits-long-for-hs256");
        properties.setAccessTokenExpirationMs(60_000);
        properties.setRefreshTokenExpirationMs(120_000);
        jwtTokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    void generatedAccessTokenIsValidAndCarriesExpectedClaims() {
        var token = jwtTokenProvider.generateAccessToken(42L, "alice");

        assertThat(jwtTokenProvider.isValid(token.token())).isTrue();

        Claims claims = jwtTokenProvider.parseClaims(token.token());
        assertThat(claims.getSubject()).isEqualTo("alice");
        assertThat(claims.get("type")).isEqualTo("access");
        assertThat(claims.get("uid", Integer.class)).isEqualTo(42);
        assertThat(claims.getId()).isEqualTo(token.jti());
    }

    @Test
    void refreshTokenIsMarkedWithRefreshType() {
        var token = jwtTokenProvider.generateRefreshToken(7L, "bob");

        Claims claims = jwtTokenProvider.parseClaims(token.token());
        assertThat(claims.get("type")).isEqualTo("refresh");
    }

    @Test
    void tamperedTokenIsRejected() {
        var token = jwtTokenProvider.generateAccessToken(1L, "carol");
        String tampered = token.token().substring(0, token.token().length() - 1) + "x";

        assertThat(jwtTokenProvider.isValid(tampered)).isFalse();
    }

    @Test
    void eachGeneratedTokenGetsAUniqueId() {
        var first = jwtTokenProvider.generateAccessToken(1L, "carol");
        var second = jwtTokenProvider.generateAccessToken(1L, "carol");

        assertThat(first.jti()).isNotEqualTo(second.jti());
    }
}
