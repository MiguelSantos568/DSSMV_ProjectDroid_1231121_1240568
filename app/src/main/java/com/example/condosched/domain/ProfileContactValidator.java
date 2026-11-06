package com.example.condosched.domain;

/**
 * Validação partilhável, sem dependências de Android ou de uma Activity.
 */
public final class ProfileContactValidator {
    private ProfileContactValidator() {
    }

    public static DataError validate(String name, String email) {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 120) return DataError.INVALID_NAME;
        if (email == null || email.trim().length() > 254
                || !email.trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) return DataError.INVALID_EMAIL;
        return null;
    }
}
