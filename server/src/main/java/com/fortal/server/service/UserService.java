package com.fortal.server.service;

import com.fortal.server.dto.UserResponse;
import com.fortal.server.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserResponse> getCurrentUser(UUID userId) {
        return userRepository.findById(userId);
    }

}
