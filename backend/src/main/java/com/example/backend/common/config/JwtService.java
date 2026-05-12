package com.example.backend.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expire-seconds}")
    private long expireSeconds;

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("uid", principal.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .signWith(getKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
