package com.fortal.server.dto;

import java.util.UUID;

public record UserCredentials(
        UUID id,
        String username,
        String passwordHash
) {
}
