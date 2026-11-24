package com.fortal.server.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void shouldHashPassword() {
        String password = "MyPassword123";

        String hash = passwordService.hash(password);

        assertNotEquals(password, hash);
        assertTrue(passwordService.matches(password, hash));
    }

    @Test
    void shouldRejectIncorrectPassword() {
        String hash = passwordService.hash("MyPassword123");

        assertFalse(
                passwordService.matches("WrongPassword", hash)
        );
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String password = "MyPassword123";

        String firstHash = passwordService.hash(password);
        String secondHash = passwordService.hash(password);

        assertNotEquals(firstHash, secondHash);
        assertTrue(passwordService.matches(password, firstHash));
        assertTrue(passwordService.matches(password, secondHash));
    }
}
