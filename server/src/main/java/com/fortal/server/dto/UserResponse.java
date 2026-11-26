package com.fortal.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        @JsonProperty("created_at") Instant createdAt
) {
}
