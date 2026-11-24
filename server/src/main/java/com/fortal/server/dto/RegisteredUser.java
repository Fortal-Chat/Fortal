package com.fortal.server.dto;

import java.util.UUID;

public record RegisteredUser(
        UUID id,
        String username
) {
}
