package br.com.volta.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordResetServiceTest {

    @BeforeEach
    void resetState() {
        PasswordResetService.clearForTests();
    }

    @Test
    void createsAndConsumesTokenOnce() {
        String token = PasswordResetService.create(10);
        assertNotNull(token);
        assertTrue(PasswordResetService.isValid(token));
        assertEquals(10, PasswordResetService.validate(token));
        assertEquals(10, PasswordResetService.consume(token));
        assertFalse(PasswordResetService.isValid(token));
        assertNull(PasswordResetService.consume(token));
    }

    @Test
    void rejectsInvalidTokens() {
        assertFalse(PasswordResetService.isValid(null));
        assertFalse(PasswordResetService.isValid(""));
        assertNull(PasswordResetService.validate("token-inexistente"));
        assertNull(PasswordResetService.consume("token-inexistente"));
        assertNull(PasswordResetService.create(null));
    }

    @Test
    void appliesCooldownPerUser() {
        assertNotNull(PasswordResetService.create(20));
        assertNull(PasswordResetService.create(20));
        assertNotNull(PasswordResetService.create(21));
    }

    @Test
    void reportsRemainingCooldownSeconds() {
        assertNotNull(PasswordResetService.create(22));
        assertTrue(PasswordResetService.secondsUntilResend(22) > 0);
        assertEquals(0, PasswordResetService.secondsUntilResend(23));
        assertEquals(0, PasswordResetService.secondsUntilResend(null));
    }

    @Test
    void createsTokensForDifferentUsers() {
        String first = PasswordResetService.create(30);
        String second = PasswordResetService.create(31);
        assertNotNull(first);
        assertNotNull(second);
        assertTrue(PasswordResetService.isValid(first));
        assertTrue(PasswordResetService.isValid(second));
    }

    @Test
    void discardReleasesTokenAndCooldown() {
        String token = PasswordResetService.create(40);
        assertNotNull(token);
        PasswordResetService.discard(token);
        assertFalse(PasswordResetService.isValid(token));
        assertNotNull(PasswordResetService.create(40));
    }
}
