package com.fortal.server.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private static final Duration TOKEN_TTL = Duration.ofDays(7);
    private final SecretKey singingKey;

    public JwtService(
            @Value("${jwt.secret}") String secret
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        if(keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain 32 bytes"
            );
        }

        this.singingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UUID userId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TOKEN_TTL)))
                .signWith(singingKey, Jwts.SIG.HS256)
                .compact();
    }

    public UUID getUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(singingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return UUID.fromString(subject);
    }
}
