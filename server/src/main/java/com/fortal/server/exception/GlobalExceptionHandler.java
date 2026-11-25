package com.fortal.server.exception;

import com.fortal.server.dto.ErrorResponse;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                                "validation_error",
                                "Invalid registration data"
                        )
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                                "validation_error",
                                "Invalid request body"
                        )
                );
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateKeyException exception
    ) {
        Throwable cause = exception;

        while (cause != null) {
            if(cause instanceof PSQLException pgException) {
                var serverError = pgException.getServerErrorMessage();

                if(serverError != null){
                    String constraint = serverError.getConstraint();

                    if("users_username_key".equals(constraint)) {
                        return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(
                                        "username_taken",
                                        "Username is already taken"
                                ));
                    }

                    if ("users_email_key".equals(constraint)) {
                        return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(
                                        "email_taken",
                                        "Email is already taken"
                                ));
                    }
                }
            }
            cause = cause.getCause();
        }
        throw exception;
    }

    @ExceptionHandler(InvalidRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRegistration(
            InvalidRegistrationException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "validation_error",
                        "Invalid registration data"
                ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ErrorResponse.of(
                                "invalid_credentials",
                                "Invalid login or password"
                        )
                );
    }
}
