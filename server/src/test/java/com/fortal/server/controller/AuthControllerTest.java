package com.fortal.server.controller;

import com.fortal.server.BaseIntegrationTest;
import com.fortal.server.security.PasswordService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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


    @Test
    void shouldLoginWithUsername() throws Exception {
        UUID id = UUID.randomUUID();

        String username = "user_" +
                id.toString().substring(0, 8);

        String email = id + "@example.com";
        String password = "password123";

        String registerJson = """
            {
                "username": "%s",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(username, email, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        String loginJson = """
            {
                "login": "%s",
                "password": "%s"
            }
            """.formatted(username, password);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").exists())
                .andExpect(jsonPath("$.user.username")
                        .value(username))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void shouldLoginWithEmail() throws Exception {
        UUID id = UUID.randomUUID();

        String username = "user_" +
                id.toString().substring(0, 8);

        String email = id + "@example.com";

        String registerJson = """
            {
                "username": "%s",
                "email": "%s",
                "password": "password123"
            }
            """.formatted(username, email);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        String loginJson = """
            {
                "login": "%s",
                "password": "password123"
            }
            """.formatted(email);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username")
                        .value(username))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {
        UUID id = UUID.randomUUID();

        String username = "user_" +
                id.toString().substring(0, 8);

        String registerJson = """
            {
                "username": "%s",
                "email": "%s",
                "password": "password123"
            }
            """.formatted(username, id + "@example.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        String loginJson = """
            {
                "login": "%s",
                "password": "wrongpassword"
            }
            """.formatted(username);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("invalid_credentials"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void shouldRejectUnknownLogin() throws Exception {
        String login = "unknown_" +
                UUID.randomUUID().toString().substring(0, 8);

        String json = """
            {
                "login": "%s",
                "password": "password123"
            }
            """.formatted(login);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("invalid_credentials"))
                .andExpect(jsonPath("$.error.message")
                        .value("Invalid login or password"));
    }

    @Test
    void shouldRejectInvalidLoginRequest() throws Exception {
        String json = """
            {
                "login": "",
                "password": ""
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code")
                        .value("validation_error"));
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "1234567",
            "пароль123",
            "password 123",
            "password+123",
            "password:123"
    })
    void shouldRejectInvalidPasswords(String password) throws Exception {
        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        String json = """
            {
                "username": "user_%s",
                "email": "%s@example.com",
                "password": "%s"
            }
            """.formatted(suffix, suffix, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code")
                        .value("validation_error"));
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "12345678",
            "Password_123",
            "Password.123",
            "Password-123",
            "Password?123",
            "Password/123",
            "Password%123",
            "Password*123",
            "Password@123",
            "Password$123",
            "Password#123",
            "Password~123",
            "Password!123",
            "Password(123)"
    })
    void shouldAcceptValidPasswords(String password) throws Exception {
        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        String username = "user_" + suffix;

        String json = """
            {
                "username": "%s",
                "email": "%s@example.com",
                "password": "%s"
            }
            """.formatted(username, suffix, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.username")
                        .value(username))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldAcceptPasswordWith72Characters() throws Exception {
        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        String json = """
            {
                "username": "user_%s",
                "email": "%s@example.com",
                "password": "%s"
            }
            """.formatted(suffix, suffix, "a".repeat(72));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }


    @Test
    void shouldTrimUsernameAndEmailOnRegistration() throws Exception {
        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        String username = "Test.User-" + suffix;
        String email = "Test_" + suffix + "@Example.com";

        String json = """
            {
                "username": "  %s  ",
                "email": "  %s  ",
                "password": "password123"
            }
            """.formatted(username, email);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.username")
                        .value(username));

        String storedEmail = jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE username = ?",
                String.class,
                username
        );

        assertEquals(email, storedEmail);
    }


    @Test
    void shouldTrimLoginButNotPassword() throws Exception {
        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        String username = "User_" + suffix;

        String registerJson = """
            {
                "username": "%s",
                "email": "%s@example.com",
                "password": "password123"
            }
            """.formatted(username, suffix);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        String validLoginJson = """
            {
                "login": "  %s  ",
                "password": "password123"
            }
            """.formatted(username);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        String invalidLoginJson = """
            {
                "login": "%s",
                "password": " password123 "
            }
            """.formatted(username);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLoginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("invalid_credentials"));
    }

}
