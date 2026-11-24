package com.fortal.server.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void shouldAcceptValidRegistration() {
        RegisterRequest request = new RegisterRequest(
                "nick123",
                "nick@example.com",
                "password123"
        );

        var violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectShortUsername() {
        RegisterRequest request = new RegisterRequest(
                "abc",
                "nick@example.com",
                "password123"
        );

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldRejectInvalidEmail() {
        RegisterRequest request = new RegisterRequest(
                "nick123",
                "invalid-email",
                "password123"
        );

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldRejectShortPassword() {
        RegisterRequest request = new RegisterRequest(
                "nick123",
                "nick@example.com",
                "123"
        );

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}
