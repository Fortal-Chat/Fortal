package com.fortal.server.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService createService() {
        SecretKey key = Jwts.SIG.HS256.key().build();

        String secret = Encoders.BASE64.encode(key.getEncoded());

        return new JwtService(secret);
    }

    @Test
    void shouldGenerateAndVerifyToken() {
        JwtService jwtService = createService();
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId);

        assertNotNull(token);
        assertEquals(userId, jwtService.getUserId(token));
    }

    @Test void shouldRejectToken() {
        JwtService jwtService = createService();
        String token = jwtService.generateToken(UUID.randomUUID());

        //Портит токен (меняет один символ)
        String[] parts = token.split("\\.");
        parts[1] = parts[1].charAt(0) == 'A'
                ? "B" + parts[1].substring(1)
                : "A" + parts[1].substring(1);

        String wrongToken = String.join(".", parts);

        assertThrows(
                JwtException.class,
                () -> jwtService.getUserId(wrongToken)
        );
    }

    @Test
    void shouldRejectInvalidBase64Secret() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService("not a valid base64 key!")
        );

        assertEquals(
                "JWT secret must be Base64-encoded and contain exactly 32 bytes",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectShortSecret() {
        String shortSecret = Encoders.BASE64.encode(
                new byte[16]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(shortSecret)
        );

        assertEquals(
                "JWT secret must be Base64-encoded and contain exactly 32 bytes",
                exception.getMessage()
        );
    }

}
