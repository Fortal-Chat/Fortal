package com.fortal.server.service;

import com.fortal.server.dto.AuthResponse;
import com.fortal.server.dto.RegisterRequest;
import com.fortal.server.exception.InvalidRegistrationException;
import com.fortal.server.repository.UserRepository;
import com.fortal.server.security.JwtService;
import com.fortal.server.security.PasswordService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
