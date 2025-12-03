package com.vahabvahabov.AI_Powered_Question_Generation_Module.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
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
        if (requestURI.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = getUsernameFromRequest(request);
        if (username == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String rateLimitKey = "rate_limit:" + username;
        String windowKey = "rate_window:" + username;

        try {
            Long currentWindow = (Long) redisTemplate.opsForValue().get(windowKey);
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
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Rate limit exceeded\", \"message\": \"Too many requests. Please try again later.\"}");
                log.warn("Rate limit exceeded for user: {}", username);
                return;
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in rate limiting filter", e);
            filterChain.doFilter(request, response);
        }
    }

    private String getUsernameFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                return "user_" + request.getRemoteAddr();
            } catch (Exception e) {
                log.debug("Could not extract username from request");
            }
        }
        return null;
    }
}