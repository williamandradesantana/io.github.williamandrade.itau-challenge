package io.github.williamandradesantana.itau_challenge.domain.auth.user;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidPasswordException;

import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_BYTES = 72;

    private PasswordPolicy() {}

    public static void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank())
            throw new InvalidPasswordException("Password cannot be null or blank");
        if (rawPassword.length() < MIN_LENGTH)
            throw new InvalidPasswordException("The password must have at least " + MIN_LENGTH + " characters");
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES)
            throw new InvalidPasswordException("The password cannot exceed " + MAX_BYTES + " bytes");
    }

}
