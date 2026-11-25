package com.fortal.server.controller;

import com.fortal.server.BaseIntegrationTest;
import com.fortal.server.security.PasswordService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordService passwordService;

    @Test
    void shouldRegisterUser() throws Exception {
        String username = "user_" +
                UUID.randomUUID().toString().substring(0, 8);

        String email = UUID.randomUUID() + "@example.com";

        String json = """
                {
                    "username": "%s",
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(username, email);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id").exists())
                .andExpect(jsonPath("$.user.username").value(username))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.password").doesNotExist());

        String storedHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE username = ?",
                String.class,
                username
        );

        assertNotNull(storedHash);
        assertNotEquals("password123", storedHash);
        assertTrue(passwordService.matches("password123", storedHash));
    }

    @Test
    void shouldRejectInvalidRegistration() throws Exception {
        String json = """
                {
                    "username": "ab",
                    "email": "wrong-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code")
                        .value("validation_error"));
    }

    @Test
    void shouldRejectTooLongPassword() throws Exception {
        String password = "a".repeat(73);

        String json = """
                {
                    "username": "valid_user",
                    "email": "valid@example.com",
                    "password": "%s"
                }
                """.formatted(password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code")
                        .value("validation_error"));
    }

    @Test
    void shouldRejectDuplicateUsername() throws Exception {
        String username = "user_" +
                UUID.randomUUID().toString().substring(0, 8);

        String firstJson = """
                {
                    "username": "%s",
                    "email": "first_%s@example.com",
                    "password": "password123"
                }
                """.formatted(username, UUID.randomUUID());

        String secondJson = """
                {
                    "username": "%s",
                    "email": "second_%s@example.com",
                    "password": "password123"
                }
                """.formatted(username, UUID.randomUUID());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code")
                        .value("username_taken"));
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        String email = UUID.randomUUID() + "@example.com";

        String firstJson = """
                {
                    "username": "first_%s",
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(
                UUID.randomUUID().toString().substring(0, 8),
                email
        );

        String secondJson = """
                {
                    "username": "second_%s",
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(
                UUID.randomUUID().toString().substring(0, 8),
                email
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code")
                        .value("email_taken"));
    }
}
