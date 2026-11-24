package com.fortal.server.repository;

import com.fortal.server.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveUser() {
        UUID id = UUID.randomUUID();

        userRepository.save(
                id,
                "testuser_" + id.toString().substring(0, 8),
                id + "@example.com",
                "test_hash"
        );

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Integer.class,
                id
        );

        assertEquals(1, count);
    }

    @Test
    void shouldRejectDuplicateUsername() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        String username = "user_" +
                firstId.toString().substring(0, 8);

        userRepository.save(
                firstId,
                username,
                firstId + "@example.com",
                "test_hash"
        );

        assertThrows(
                DuplicateKeyException.class,
                () -> userRepository.save(
                        secondId,
                        username,
                        secondId + "@example.com",
                        "test_hash"
                )
        );
    }

    @Test
    void shouldRejectDuplicateEmail() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        String email = firstId + "@example.com";

        userRepository.save(
                firstId,
                "user_" + firstId.toString().substring(0, 8),
                email,
                "test_hash"
        );

        assertThrows(
                DuplicateKeyException.class,
                () -> userRepository.save(
                        secondId,
                        "user_" + secondId.toString().substring(0, 8),
                        email,
                        "test_hash"
                )
        );
    }
}
