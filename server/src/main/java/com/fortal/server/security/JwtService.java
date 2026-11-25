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
    private final SecretKey signingKey;


    public JwtService(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes;

        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "JWT secret must be Base64-encoded and contain exactly 32 bytes",
                    exception
            );
        }

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be Base64-encoded and contain exactly 32 bytes"
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateToken(UUID userId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TOKEN_TTL)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public UUID getUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return UUID.fromString(subject);
    }
}
