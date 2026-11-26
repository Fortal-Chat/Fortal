package com.fortal.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 4, max = 32)
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$")
        String username,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(
                regexp = "^[a-zA-Z0-9_.?/\\\\%*@$#~!()-]{8,72}$"
        )
        String password
) {

        public RegisterRequest {
                if (username != null) {
                        username = username.trim();
                }

                if (email != null) {
                        email = email.trim();
                }
        }
}
