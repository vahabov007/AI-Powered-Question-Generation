package com.vahabvahabov.AI_Powered_Question_Generation_Module.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/auth/",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/swagger-ui.html",
            "/api-docs/",
            "/webjars/",
            "/favicon.ico",
            "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                log.warn("JWT token expired: {}", e.getMessage());
                handleJwtError(response, "Token expired", 401);
                return;
            } catch (MalformedJwtException e) {
                log.warn("Invalid JWT token format: {}", e.getMessage());
                handleJwtError(response, "Invalid token format", 401);
                return;
            } catch (SignatureException e) {
                log.warn("Invalid JWT signature: {}", e.getMessage());
                handleJwtError(response, "Invalid token signature", 401);
                return;
            } catch (Exception e) {
                log.error("Error processing JWT token: {}", e.getMessage());
                handleJwtError(response, "Error processing token", 401);
                return;
            }
        } else {
            log.debug("No Authorization header or invalid format for request: {}", requestURI);
            handleJwtError(response, "Missing or invalid Authorization header", 401);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("Successfully authenticated user: {}", username);
                } else {
                    log.warn("JWT token validation failed for user: {}", username);
                    handleJwtError(response, "Token validation failed", 401);
                    return;
                }
            } catch (Exception e) {
                log.error("Error loading user details for {}: {}", username, e.getMessage());
                handleJwtError(response, "Error loading user details", 401);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestURI) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(requestURI::startsWith) ||
                requestURI.equals("/") ||
                requestURI.endsWith(".css") ||
                requestURI.endsWith(".js") ||
                requestURI.endsWith(".png") ||
                requestURI.endsWith(".jpg") ||
                requestURI.endsWith(".ico");
    }

    private void handleJwtError(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Authentication Error\", \"message\": \"" + message + "\"}");
    }
}