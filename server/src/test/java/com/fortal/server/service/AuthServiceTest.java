package com.fortal.server.service;

import com.fortal.server.dto.AuthResponse;
import com.fortal.server.dto.LoginRequest;
import com.fortal.server.dto.RegisterRequest;
import com.fortal.server.dto.UserCredentials;
import com.fortal.server.exception.InvalidCredentialsException;
import com.fortal.server.exception.InvalidRegistrationException;
import com.fortal.server.repository.UserRepository;
import com.fortal.server.security.JwtService;
import com.fortal.server.security.PasswordService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest(
                "nick123",
                "nick@example.com",
                "password123"
        );

        when(passwordService.hash("password123"))
                .thenReturn("hashed_password");

        when(jwtService.generateToken(any(UUID.class)))
                .thenReturn("test.jwt.token");

        AuthResponse response = authService.register(request);

        assertNotNull(response.user().id());
        assertEquals("nick123", response.user().username());
        assertEquals("test.jwt.token", response.token());

        verify(passwordService).hash("password123");

        verify(userRepository).save(
                eq(response.user().id()),
                eq("nick123"),
                eq("nick@example.com"),
                eq("hashed_password")
        );

        verify(jwtService).generateToken(
                eq(response.user().id())
        );
    }

    @Test
    void shouldRejectPasswordExceedingBcryptLimit() {
        String longPassword = "a".repeat(73);

        RegisterRequest request = new RegisterRequest(
                "nick123",
                "nick@example.com",
                longPassword
        );

        assertThrows(
                InvalidRegistrationException.class,
                () -> authService.register(request)
        );

        verifyNoInteractions(
                userRepository,
                passwordService,
                jwtService
        );
    }

    @Test
    void shouldLoginWithUsername() {
        UUID userId = UUID.randomUUID();

        LoginRequest request = new LoginRequest(
                "alice",
                "password123"
        );

        UserCredentials credentials = new UserCredentials(
                userId,
                "alice",
                "stored_hash"
        );

        when(userRepository.findByLogin("alice"))
                .thenReturn(Optional.of(credentials));

        when(passwordService.matches("password123", "stored_hash"))
                .thenReturn(true);

        when(jwtService.generateToken(userId))
                .thenReturn("test.jwt.token");

        AuthResponse response = authService.login(request);

        assertEquals(userId, response.user().id());
        assertEquals("alice", response.user().username());
        assertEquals("test.jwt.token", response.token());

        verify(passwordService)
                .matches("password123", "stored_hash");

        verify(jwtService).generateToken(userId);
    }

    @Test
    void shouldLoginWithEmail() {
        UUID userId = UUID.randomUUID();

        LoginRequest request = new LoginRequest(
                "alice@example.com",
                "password123"
        );

        UserCredentials credentials = new UserCredentials(
                userId,
                "alice",
                "stored_hash"
        );

        when(userRepository.findByLogin("alice@example.com"))
                .thenReturn(Optional.of(credentials));

        when(passwordService.matches("password123", "stored_hash"))
                .thenReturn(true);

        when(jwtService.generateToken(userId))
                .thenReturn("email.jwt.token");

        AuthResponse response = authService.login(request);

        assertEquals(userId, response.user().id());
        assertEquals("alice", response.user().username());
        assertEquals("email.jwt.token", response.token());

        verify(userRepository).findByLogin("alice@example.com");
    }

    @Test
    void shouldRejectWrongPassword() {
        UUID userId = UUID.randomUUID();

        LoginRequest request = new LoginRequest(
                "alice",
                "wrongpassword"
        );

        UserCredentials credentials = new UserCredentials(
                userId,
                "alice",
                "stored_hash"
        );

        when(userRepository.findByLogin("alice"))
                .thenReturn(Optional.of(credentials));

        when(passwordService.matches("wrongpassword", "stored_hash"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(jwtService);
    }


    @Test
    void shouldRejectUnknownUser() {
        LoginRequest request = new LoginRequest(
                "unknown_user",
                "password123"
        );

        when(userRepository.findByLogin("unknown_user"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(passwordService, jwtService);
    }

}
