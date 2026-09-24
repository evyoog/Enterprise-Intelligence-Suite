package com.vyoog.eisplatform.modules.registration.service;

/**
 * No password policy exists anywhere else in this codebase (this backend
 * never validates a password of its own — Keycloak is the sole credential
 * store, see KeycloakPasswordGrantService). This is a conservative floor
 * applied before a registration is even accepted, not a replacement for
 * whatever policy the Keycloak realm itself enforces once an account is
 * eventually linked there.
 */
public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;

    private PasswordPolicy() {
    }

    public static void validate(String password, String confirmPassword) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one letter and one number");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }
}
