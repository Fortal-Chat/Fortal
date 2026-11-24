package com.fortal.server.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(
            UUID id,
            String username,
            String email,
            String passwordHash
    ) {
        String sql = """
                INSERT INTO users (
                    id,
                    username,
                    email,
                    password_hash
                )
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                id,
                username,
                email,
                passwordHash
        );
    }
}
