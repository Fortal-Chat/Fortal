package com.fortal.server.controller;

import com.fortal.server.BaseIntegrationTest;
import com.fortal.server.security.JwtService;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldReturnCurrentUser() throws Exception {
        UUID userId = UUID.randomUUID();

        String username = "user_" +
                userId.toString().substring(0, 8);

        String email = userId + "@example.com";

        insertUser(userId, username, email);

        String token = jwtService.generateToken(userId);

        String response = mockMvc.perform(get("/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.created_at").isString())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createdAt = JsonPath.read(response, "$.created_at");

        assertNotNull(Instant.parse(createdAt));
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("unauthorized"));
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer invalid-token"
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("unauthorized"));
    }

    @Test
    void shouldRejectTokenForNonexistentUser() throws Exception {
        UUID nonexistentUserId = UUID.randomUUID();

        String token = jwtService.generateToken(nonexistentUserId);

        mockMvc.perform(get("/users/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + token
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("unauthorized"));
    }

    @Test
    void shouldReturnOnlyAuthenticatedUser() throws Exception {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        insertUser(firstId, "first_" + firstId.toString().substring(0, 8),
                firstId + "@example.com");

        insertUser(secondId, "second_" + secondId.toString().substring(0, 8),
                secondId + "@example.com");

        String firstToken = jwtService.generateToken(firstId);
        String secondToken = jwtService.generateToken(secondId);

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstId.toString()))
                .andExpect(jsonPath("$.email")
                        .value(firstId + "@example.com"));

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + secondToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(secondId.toString()))
                .andExpect(jsonPath("$.email")
                        .value(secondId + "@example.com"));
    }

    private void insertUser(UUID id, String username, String email) {
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, email, password_hash)
                VALUES (?, ?, ?, ?)
                """,
                id,
                username,
                email,
                "test-password-hash"
        );
    }
}
