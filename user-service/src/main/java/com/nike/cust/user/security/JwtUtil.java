package com.nike.cust.user.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final String ISSUER   = "quickbuy-auth-service";
    private static final String AUDIENCE = "quickbuy-api";

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-hours:24}") long expirationHours) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationHours * 3600 * 1000;
    }

    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                // --- OAuth 2.0 / RFC 7519 standard claims ---
                .id(UUID.randomUUID().toString())           // jti — unique token ID
                .issuer(ISSUER)                             // iss — who issued the token
                .audience().add(AUDIENCE).and()             // aud — intended recipient
                .subject(String.valueOf(userId))            // sub — user identity
                .issuedAt(now)                              // iat
                .expiration(new Date(now.getTime() + expirationMs)) // exp
                // --- application claims ---
                .claim("email", email)
                .claim("role", role)
                .claim("authorities", List.of("ROLE_" + role))
                .signWith(secretKey)
                .compact();
    }
}
