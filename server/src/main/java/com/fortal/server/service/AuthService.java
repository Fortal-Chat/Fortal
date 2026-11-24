package com.fortal.server.service;

import com.fortal.server.dto.AuthResponse;
import com.fortal.server.dto.RegisterRequest;
import com.fortal.server.dto.RegisteredUser;
import com.fortal.server.exception.InvalidRegistrationException;
import com.fortal.server.repository.UserRepository;
import com.fortal.server.security.JwtService;
import com.fortal.server.security.PasswordService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordService passwordService,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidRegistrationException(
                    "Password exceeds BCrypt limit"
            );
        }
        UUID id = UUID.randomUUID();
        String passwordHash = passwordService.hash(request.password());

        userRepository.save(
                id,
                request.username(),
                request.email(),
                passwordHash
        );
        RegisteredUser user = new RegisteredUser(
                id,
                request.username()
        );
        String token = jwtService.generateToken(id);
        return new AuthResponse(user, token);
    }
}
