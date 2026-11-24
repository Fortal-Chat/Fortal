package com.fortal.server.dto;

public record AuthResponse(
        RegisteredUser user,
        String token
){
}
