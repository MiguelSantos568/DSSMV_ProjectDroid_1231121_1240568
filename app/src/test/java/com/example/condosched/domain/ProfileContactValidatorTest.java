package com.example.condosched.domain;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProfileContactValidatorTest {
    @Test
    public void acceptsContactWithWhitespaceAroundIt() {
        assertNull(ProfileContactValidator.validate("  Casa A  ", " casa@example.com "));
    }

    @Test
    public void rejectsMissingAndOversizedName() {
        assertEquals(DataError.INVALID_NAME, ProfileContactValidator.validate(" ", "casa@example.com"));
        assertEquals(DataError.INVALID_NAME, ProfileContactValidator.validate("x".repeat(121), "casa@example.com"));
    }

    @Test
    public void rejectsMalformedEmail() {
        for (String email : new String[]{"", "casa", "a@@example.com", "a b@example.com", "a@"}) {
            assertEquals(DataError.INVALID_EMAIL, ProfileContactValidator.validate("Casa", email));
        }
    }
}
