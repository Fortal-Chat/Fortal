package com.fortal.server.security;

import com.fortal.server.BaseIntegrationTest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Import(JwtAuthenticationIntegrationTest.ProtectedTestController.class)
class JwtAuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @RestController
    public static class ProtectedTestController {

        @GetMapping("/test/protected")
        public Map<String, String> protectedEndpoint(
                @AuthenticationPrincipal UUID userId
        ) {
            return Map.of("userId", userId.toString());
        }
    }

    @Test
    void shouldAllowValidJwt() throws Exception {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId);

        mockMvc.perform(get("/test/protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void shouldRejectMissingJwt() throws Exception {
        mockMvc.perform(get("/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("unauthorized"));
    }

    @Test
    void shouldRejectInvalidJwt() throws Exception {
        mockMvc.perform(get("/test/protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("unauthorized"));
    }

    @Test
    void shouldRejectExpiredJwt() throws Exception {
        Instant now = Instant.now();

        String expiredToken = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(now.minusSeconds(3600)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(
                        Keys.hmacShaKeyFor(
                                Decoders.BASE64.decode(jwtSecret)
                        ),
                        Jwts.SIG.HS256
                )
                .compact();

        mockMvc.perform(get("/test/protected")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("unauthorized"));
    }

    @Test
    void shouldNotReuseAuthenticationBetweenRequests() throws Exception {
        UUID firstUser = UUID.randomUUID();
        UUID secondUser = UUID.randomUUID();

        String firstToken = jwtService.generateToken(firstUser);
        String secondToken = jwtService.generateToken(secondUser);

        mockMvc.perform(get("/test/protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(firstUser.toString()));

        mockMvc.perform(get("/test/protected"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/test/protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(secondUser.toString()));
    }

    @Test
    void shouldAllowPublicHealthWithoutJwt() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void shouldRejectJwtWithWrongSignature() throws Exception {
        byte[] wrongKey = new byte[32];
        java.util.Arrays.fill(wrongKey, (byte) 1);

        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(
                        Keys.hmacShaKeyFor(wrongKey),
                        Jwts.SIG.HS256
                )
                .compact();

        mockMvc.perform(get("/test/protected")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("unauthorized"));
    }

}
