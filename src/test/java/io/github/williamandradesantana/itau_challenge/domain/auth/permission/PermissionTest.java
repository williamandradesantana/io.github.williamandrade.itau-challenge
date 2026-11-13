package io.github.williamandradesantana.itau_challenge.domain.auth.permission;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.exceptions.InvalidPermissionNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PermissionTest {

    private PermissionName common;
    private PermissionName admin;
    private Permission commonPermission;
    private Permission adminPermission;
    private Set<Permission> permissions = new HashSet<>();

    @BeforeEach
    void setUp() {
        common = new PermissionName("COMMON");
        admin = new PermissionName("ADMIN");
        commonPermission = Permission.create(common);
        adminPermission = Permission.create(admin);
        permissions.add(commonPermission);
        permissions.add(adminPermission);
    }

    @Test
    @DisplayName("Test: When a permission is created, should return corrected values")
    void test_WhenAPermissionIsCreated_ShouldReturnCorrectedValues() {
        assertEquals(common, commonPermission.permissionName());
        assertEquals(admin, adminPermission.permissionName());
    }

    @Test
    @DisplayName("Test: When a permission name is null, should throw exception")
    void test_WhenAPermissionIsNull_ShouldThrowException() {
        String expectedMessage = "The permission name cannot be null or blank";
        InvalidPermissionNameException exception = assertThrows(
            InvalidPermissionNameException.class, () -> Permission.create(new PermissionName(null))
        );
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Test: When a permission name is blank, should throw exception")
    void test_WhenAPermissionIsBlank_ShouldThrowException() {
        String expectedMessage = "The permission name cannot be null or blank";
        InvalidPermissionNameException exception = assertThrows(
            InvalidPermissionNameException.class, () -> Permission.create(new PermissionName(" "))
        );
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Test: When a permission name exceed 20 characters, should throw exception")
    void test_WhenAPermissionExceedTwentyCharacters_ShouldThrowException() {
        String expectedMessage = "The permission name cannot exceed 20 characters";
        InvalidPermissionNameException exception = assertThrows(
                InvalidPermissionNameException.class, () ->
                        Permission.create(new PermissionName("1".repeat(21)))
        );
        assertEquals(expectedMessage, exception.getMessage());
    }
}