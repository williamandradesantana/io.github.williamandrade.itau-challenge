package io.github.williamandradesantana.itau_challenge.domain.auth.permission;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.exceptions.InvalidPermissionNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionNameTest {

    private PermissionName permissionName;

    @Test
    @DisplayName("Test: Creating a permission name valid, should return corrected values")
    void test_CreatingAPermissionNameValid_ShouldReturnCorrectedValues() {
        String expectedPermissionName = "COMMON";
        permissionName = new PermissionName("COMMON");

        assertEquals(expectedPermissionName, permissionName.value());
    }

    @Test
    @DisplayName("Test: when the permission name is blank, should throw exception")
    void test_WhenThePermissionNameIsBlank_ShouldThrowException() {
        String expectedMessage = "The permission name cannot be null or blank";
        InvalidPermissionNameException exception = assertThrows(
            InvalidPermissionNameException.class, () -> new PermissionName(" ")
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Test: when the permission name is null, should throw exception")
    void test_WhenThePermissionNameIsNull_ShouldThrowException() {
        String expectedMessage = "The permission name cannot be null or blank";
        InvalidPermissionNameException exception = assertThrows(
            InvalidPermissionNameException.class, () -> new PermissionName(null)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Test: when the permission name exceed twenty characters, should throw exception")
    void test_WhenThePermissionNameExceedTwentyCharacters_ShouldThrowException() {
        String expectedMessage = "The permission name cannot exceed 20 characters";
        InvalidPermissionNameException exception = assertThrows(
            InvalidPermissionNameException.class, () -> new PermissionName("1".repeat(21))
        );

        assertEquals(expectedMessage, exception.getMessage());
    }
}