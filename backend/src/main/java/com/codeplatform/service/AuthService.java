package com.codeplatform.service;

import com.codeplatform.common.Mapper;
import com.codeplatform.dto.AuthResponse;
import com.codeplatform.dto.LoginRequest;
import com.codeplatform.dto.RegisterRequest;
import com.codeplatform.entity.User;
import com.codeplatform.exception.BadRequestException;
import com.codeplatform.exception.UnauthorizedException;
import com.codeplatform.repository.UserRepository;
import com.codeplatform.security.JwtTokenProvider;
import com.codeplatform.security.RedisTokenService;
import com.codeplatform.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;
    private final Mapper mapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));

        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.isValid(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        if (!"refresh".equals(claims.get("type"))) {
            throw new UnauthorizedException("Token is not a refresh token");
        }

        Long userId = Long.valueOf(claims.get("uid").toString());
        String jti = claims.getId();

        if (!redisTokenService.isRefreshTokenValid(userId, jti)) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));

        // Rotate: invalidate the used refresh token and issue a brand new pair.
        redisTokenService.revokeRefreshToken(userId, jti);
        return issueTokens(user);
    }

    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && jwtTokenProvider.isValid(accessToken)) {
            Claims claims = jwtTokenProvider.parseClaims(accessToken);
            redisTokenService.blacklistAccessToken(claims.getId(), claims.getExpiration().toInstant());
        }
        if (refreshToken != null && jwtTokenProvider.isValid(refreshToken)) {
            Claims claims = jwtTokenProvider.parseClaims(refreshToken);
            Long userId = Long.valueOf(claims.get("uid").toString());
            redisTokenService.revokeRefreshToken(userId, claims.getId());
        }
    }

    private AuthResponse issueTokens(User user) {
        var access = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        var refresh = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        redisTokenService.whitelistRefreshToken(user.getId(), refresh.jti(), refresh.expiresAt().toInstant());

        return AuthResponse.builder()
                .accessToken(access.token())
                .refreshToken(refresh.token())
                .tokenType("Bearer")
                .user(mapper.toUserResponse(user))
                .build();
    }
}
