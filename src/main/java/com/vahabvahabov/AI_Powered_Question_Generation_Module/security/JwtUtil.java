package com.vahabvahabov.AI_Powered_Question_Generation_Module.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expirationTime;

    private SecretKey getSigningKey() {
        String safeSecret = secret;
        if (safeSecret.length() < 64) {
            safeSecret = String.format("%-64s", safeSecret).replace(' ', '0');
        } else if (safeSecret.length() > 64) {
            safeSecret = safeSecret.substring(0, 64);
        }
        return Keys.hmacShaKeyFor(safeSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws JwtException {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getExpiration();
        } catch (JwtException e) {
            logger.error("Error extracting expiration from token", e);
            throw e;
        }
    }

    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            logger.error("Error checking token expiration", e);
            return true;
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            logger.error("Error extracting username from token", e);
            throw e;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            if (isTokenExpired(token)) {
                logger.warn("Token expired for user: {}", username);
                return false;
            }

            boolean usernameMatches = username.equals(userDetails.getUsername());

            if (!usernameMatches) {
                logger.warn("Token username mismatch. Token: {}, UserDetails: {}", username, userDetails.getUsername());
            }
            extractAllClaims(token);

            return usernameMatches;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired for user: {}", e.getClaims().getSubject());
            return false;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature", e);
            return false;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token", e);
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token", e);
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty", e);
            return false;
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return false;
        }
    }

    public Date getExpirationTime(String token) {
        return extractExpiration(token);
    }

    public String refreshToken(String token) {
        try {
            final Claims claims = extractAllClaims(token);
            return createToken(claims, claims.getSubject());
        } catch (JwtException e) {
            logger.error("Cannot refresh token", e);
            throw e;
        }
    }
}