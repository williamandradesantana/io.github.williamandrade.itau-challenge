package io.github.williamandradesantana.itau_challenge.domain.auth.user;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordPolicyTest {

    @Test
    @DisplayName("Test: accept a password valid")
    void test_AcceptAPasswordValid() {
        String password = "password-test";
        assertDoesNotThrow(() -> PasswordPolicy.validate(password));
    }

    @Test
    @DisplayName("Test: when password is blank, should throw exception")
    void test_WhenPasswordIsBlank_ShouldThrowException() {
        assertThrows(InvalidPasswordException.class, () -> PasswordPolicy.validate(""));
    }

    @Test
    @DisplayName("Test: when password is null, should throw exception")
    void test_WhenPasswordIsNull_ShouldThrowException() {
        assertThrows(InvalidPasswordException.class, () -> PasswordPolicy.validate(null));
    }

    @Test
    @DisplayName("Test: when password have less than 8 characters, should throw exception")
    void test_WhenPasswordHaveLessThan8Characters_ShouldThrowException() {
        assertThrows(InvalidPasswordException.class, () -> PasswordPolicy.validate("1".repeat(7)));
    }

    @Test
    @DisplayName("Test: when password exceeded 72 UTF-8 bytes, should throw exception")
    void test_WhenPasswordExceeded72UTF8Bytes_ShouldThrowException() {
        assertThrows(InvalidPasswordException.class, () -> PasswordPolicy.validate("1".repeat(73)));
    }
}