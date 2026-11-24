package com.fortal.server.dto;

public record ErrorResponse(
        ErrorDetails error
) {
    public record ErrorDetails(
            String code,
            String message
    ) {
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(
                new ErrorDetails(code, message)
        );
    }
}
