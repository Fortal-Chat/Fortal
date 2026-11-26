package com.fortal.server.repository;

import com.fortal.server.dto.UserCredentials;
import com.fortal.server.dto.UserResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
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

    public Optional<UserCredentials> findByLogin(String login) {
        String sql = """
                SELECT id, username, password_hash
                FROM users
                WHERE username = ? OR email = ?
                """;
        return jdbcTemplate.query(
                sql,
                rs -> {
                    if(!rs.next()) {
                        return Optional.empty();
                    }
                    UserCredentials user = new UserCredentials(
                            rs.getObject("id", UUID.class),
                            rs.getString("username"),
                            rs.getString("password_hash")
                    );
                    return Optional.of(user);
                },
                login,
                login
        );
    }

    public Optional<UserResponse> findById(UUID id) {
        String sql = """
                SELECT id, username, email, created_at
                FROM users
                WHERE id = ?
                """;
        return jdbcTemplate.query(
                sql,
                rs -> {
                    if(!rs.next()) {
                        return Optional.empty();
                    }
                    UserResponse user = new UserResponse(
                            rs.getObject("id", UUID.class),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getTimestamp("created_at").toInstant()
                    );
                    return Optional.of(user);
                },
                id
        );
    }
}
