package com.vahabvahabov.AI_Powered_Question_Generation_Module.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component @Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rate-limit.requests-per-minute:30}")
    private int requestsPerMinute;

    @Value("${rate-limit.burst-capacity:50}")
    private int burstCapacity;

    public RateLimitFilter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // Don't rate the user who tries to register
        if (requestURI.startsWith("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = getUsernameFromRequest(request);
        if (username == null || username.startsWith("guest_")) {
            SendErrorResponse(response,
                       401,
                     "Unauthorized",
                        "Authentication required to access this resource",
                              requestURI);
            return;
        }

        String rateLimitKey = "rate_limit:" + username;
        String windowKey = "rate_window:" + username;

        try {
            Object value = redisTemplate.opsForValue().get(windowKey);
            Long currentWindow = 0L;
            if (value instanceof Number) {
                currentWindow = ((Number) value).longValue();
            }
            long now = Instant.now().getEpochSecond();
            long windowSize = 60; // 1 minute in seconds

            if (currentWindow == null || now - currentWindow > windowSize) {
                redisTemplate.opsForValue().set(windowKey, now, windowSize, TimeUnit.SECONDS);
                redisTemplate.opsForValue().set(rateLimitKey, 1, windowSize, TimeUnit.SECONDS);
                filterChain.doFilter(request, response);
                return;
            }
            Long requestCount = redisTemplate.opsForValue().increment(rateLimitKey);

            if (requestCount == null) {
                requestCount = 1L;
                redisTemplate.opsForValue().set(rateLimitKey, requestCount,
                        Duration.ofSeconds(windowSize - (now - currentWindow)));
            }

            if (requestCount > burstCapacity) {
                SendErrorResponse(response,
                            429,
                         "Rate Limit Exceeded",
                            "Too many requests. Please try again later.",
                                  requestURI);
                log.warn("Rate limit exceeded for user: {}", username);
                return;
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in rate limiting filter", e);
            filterChain.doFilter(request, response);
        }
    }

    private void SendErrorResponse(HttpServletResponse response, int status, String message, String error, String requestURI) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = new ErrorResponse(
                429,
                message,
                error,
                requestURI,
                Instant.now()
        );
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private String getUsernameFromRequest(HttpServletRequest request) {
        String remoteAddr = "";
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            remoteAddr = xForwardedFor.split(",")[0].trim();
        } else {
            remoteAddr = request.getRemoteAddr();
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // In a real app, you'd extract the 'sub' (subject) from the JWT here.
            return "user_auth_" + remoteAddr;
        }
        return "guest_" + remoteAddr;
    }
}