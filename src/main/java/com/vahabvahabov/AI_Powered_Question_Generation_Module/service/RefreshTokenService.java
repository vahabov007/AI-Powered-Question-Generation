package com.vahabvahabov.AI_Powered_Question_Generation_Module.service;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.TokenResponse;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.customExceptions.CustomException;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.RefreshToken;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.RefreshTokenRepository;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.UserRepository;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-days}")
    private Long refreshExpirationDays;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        refreshToken.setUserId(user.getId());
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpirationTime(Instant.now().plus(Duration.ofDays(refreshExpirationDays)));
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpirationTime().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException("Refresh Token was expired. Please sign in again.", HttpStatus.UNAUTHORIZED);
        }
        return refreshToken;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
         return refreshTokenRepository.deleteByUser(userId);
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        RefreshToken dbToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException("Refresh token not found.", HttpStatus.NOT_FOUND));
        verifyExpiration(dbToken);

        User user = userRepository.findById(dbToken.getId())
                .orElseThrow(() -> new CustomException("User not found.", HttpStatus.NOT_FOUND));
        String roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));


        String accessToken = jwtTokenProvider.generateTokenFromUsername(user.getUsername(), roles);
        return new TokenResponse(accessToken, refreshToken);

    }
}

