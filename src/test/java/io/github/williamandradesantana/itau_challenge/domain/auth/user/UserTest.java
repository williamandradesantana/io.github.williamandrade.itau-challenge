package io.github.williamandradesantana.itau_challenge.domain.auth.user;

import io.github.williamandradesantana.itau_challenge.domain.auth.permission.Permission;
import io.github.williamandradesantana.itau_challenge.domain.auth.permission.PermissionName;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidFullNameException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidPasswordException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidUserNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private PermissionName common;
    private Permission commonPermission;
    private PermissionName admin;
    private Permission adminPermission;
    private User user;
    private Set<Permission> permissions = new HashSet<>();

    @BeforeEach
    void setUp() {
        common = new PermissionName("COMMON");
        admin = new PermissionName("ADMIN");
        commonPermission = Permission.create(common);
        adminPermission = Permission.create(admin);
        user = User.create("username", "fullName", "password", permissions);
    }

    @Test
    @DisplayName("Test: create user with success and without permissions")
    void test_CreateUserWithSuccessAndWithoutPermissions() {
        String expectedUsername = "username";
        String expectedFullName = "fullName";
        String expectedPassword = "password";
        int expectedQuantityPermissions = 0;

        assertNotNull(user, () -> "user cannot be null");
        assertEquals(expectedUsername, user.getUserName(), () -> "username not matches");
        assertEquals(expectedFullName, user.getFullName(), () -> "fullName not matches");
        assertEquals(expectedPassword, user.getPassword(), () -> "password not matches");
        assertEquals(expectedQuantityPermissions, user.getPermissions().size(), () -> "quantity permissions not matches");
    }

    @Test
    @DisplayName("Test: create user with success and with permissions")
    void test_CreateUserWithSuccessAndWithPermissions() {
        String expectedUsername = "username";
        String expectedFullName = "fullName";
        String expectedPassword = "password";
        int expectedQuantityPermissions = 2;

        user.grantPermission(adminPermission);
        user.grantPermission(commonPermission);

        assertNotNull(user, () -> "user cannot be null");
        assertEquals(expectedUsername, user.getUserName(), () -> "username not matches");
        assertEquals(expectedFullName, user.getFullName(), () -> "fullName not matches");
        assertEquals(expectedPassword, user.getPassword(), () -> "password not matches");
        assertEquals(expectedQuantityPermissions, user.getPermissions().size(), () -> "quantity permissions not matches");
        assertTrue(user.getPermissions().contains(adminPermission));
        assertTrue(user.getPermissions().contains(commonPermission));
    }

    @Test
    @DisplayName("Test: create user with success and with one permission")
    void test_CreateUserWithSuccessAndWithOnePermission() {
        String expectedUsername = "username";
        String expectedFullName = "fullName";
        String expectedPassword = "password";
        int expectedQuantityPermissions = 1;

        user.grantPermission(adminPermission);

        assertNotNull(user, () -> "user cannot be null");
        assertEquals(expectedUsername, user.getUserName(), () -> "username not matches");
        assertEquals(expectedFullName, user.getFullName(), () -> "fullName not matches");
        assertEquals(expectedPassword, user.getPassword(), () -> "password not matches");
        assertEquals(expectedQuantityPermissions, user.getPermissions().size(), () -> "quantity permissions not matches");
        assertTrue(user.getPermissions().contains(adminPermission));
        assertFalse(user.getPermissions().contains(commonPermission));
    }

    @Test
    @DisplayName("Test: when the username has spaces at the ends, verify if trim method is working")
    void test_WhenUserNameHasSpacesBothSides_VerifyIfTrimMethodIsWorking() {
        String expectedUserName = "william";
        String expectedFullName = "fullName";
        String expectedPassword = "password";
        int expectedQuantityPermissions = 1;

        user.grantPermission(adminPermission);

        user.setUserName(" william ");


        assertNotNull(user, () -> "user cannot be null");
        assertEquals(expectedUserName, user.getUserName(), () -> "username not matches");
        assertEquals(expectedFullName, user.getFullName(), () -> "fullName not matches");
        assertEquals(expectedPassword, user.getPassword(), () -> "password not matches");
        assertEquals(expectedQuantityPermissions, user.getPermissions().size(), () -> "quantity permissions not matches");
        assertTrue(user.getPermissions().contains(adminPermission));
        assertFalse(user.getPermissions().contains(commonPermission));
    }

    @Test
    @DisplayName("Test: when username is null, should throw exception")
    void test_WhenUserNameIsNull_ShouldThrowException() {
        String expectedMessage = "User name cannot be null or blank";

        InvalidUserNameException exception = assertThrows(
            InvalidUserNameException.class,
            () -> user.setUserName(null)
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when username is blank, should throw exception")
    void test_WhenUserNameIsBlank_ShouldThrowException() {
        String expectedMessage = "User name cannot be null or blank";

        InvalidUserNameException exception = assertThrows(
            InvalidUserNameException.class,
            () -> user.setUserName(" ")
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when username less than 3 characters, should throw exception")
    void test_WhenUserNameLessThanThreeCharacters_ShouldThrowException() {
        String expectedMessage = "The user name must be between 3 and 30 characters";

        InvalidUserNameException exception = assertThrows(
            InvalidUserNameException.class,
            () -> user.setUserName("11")
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when full name is null, should throw exception")
    void test_WhenFullNameIsNull_ShouldThrowException() {
        String expectedMessage = "Full name cannot be null or blank";

        InvalidFullNameException exception = assertThrows(
                InvalidFullNameException.class,
                () -> user.setFullName(null)
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when full name is blank, should throw exception")
    void test_WhenFullNameIsBlank_ShouldThrowException() {
        String expectedMessage = "Full name cannot be null or blank";

        InvalidFullNameException exception = assertThrows(
                InvalidFullNameException.class,
                () -> user.setFullName(" ")
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when full name less than 3 characters, should throw exception")
    void test_WhenFullNameLessThanThreeCharacters_ShouldThrowException() {
        String expectedMessage = "The full name be between 3 and 150 characters";

        InvalidFullNameException exception = assertThrows(
            InvalidFullNameException.class,
            () -> user.setFullName("11")
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when full name greater than 150 characters, should throw exception")
    void test_WhenFullNameGreaterThanOneHundredAndFiftyCharacters_ShouldThrowException() {
        String expectedMessage = "The full name be between 3 and 150 characters";

        InvalidFullNameException exception = assertThrows(
            InvalidFullNameException.class,
            () -> user.setFullName("1".repeat(151))
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }


    @Test
    @DisplayName("Test: when the full name has spaces at the ends, verify if trim method is working")
    void test_WhenFullNameHasSpacesBothSides_VerifyIfTrimMethodIsWorking() {
        String expectedUserName = "username";
        String expectedFullName = "fullName";
        String expectedPassword = "password";
        int expectedQuantityPermissions = 1;

        user.grantPermission(adminPermission);

        user.setFullName(" fullName ");

        assertNotNull(user, () -> "user cannot be null");
        assertEquals(expectedUserName, user.getUserName(), () -> "username not matches");
        assertEquals(expectedFullName, user.getFullName(), () -> "fullName not matches");
        assertEquals(expectedPassword, user.getPassword(), () -> "password not matches");
        assertEquals(expectedQuantityPermissions, user.getPermissions().size(), () -> "quantity permissions not matches");
        assertTrue(user.getPermissions().contains(adminPermission));
        assertFalse(user.getPermissions().contains(commonPermission));
    }

    @Test
    @DisplayName("Test: when password is null, should throw exception")
    void test_WhenPasswordIsNull_ShouldThrowException() {
        String expectedMessage = "Password cannot be null or blank";

        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> user.setPassword(null)
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when password is blank, should throw exception")
    void test_WhenPasswordIsBlank_ShouldThrowException() {
        String expectedMessage = "Password cannot be null or blank";

        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> user.setPassword(" ")
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when password less than 8 characters, should throw exception")
    void test_WhenPasswordLessThanEightCharacters_ShouldThrowException() {
        String expectedMessage = "The password must be between 8 and 255 characters";

        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> user.setPassword("1".repeat(7))
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: when password greater than 256 characters, should throw exception")
    void test_WhenPasswordGreaterThanTwoHundredAndFiftySixCharacters_ShouldThrowException() {
        String expectedMessage = "The password must be between 8 and 255 characters";

        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> user.setPassword("1".repeat(256))
        );

        assertEquals(expectedMessage, exception.getMessage(), () -> "The message not matches");
    }

    @Test
    @DisplayName("Test: updating a user with successfully")
    void test_UpdatingAUserWithSuccessfully() {
        String expectedUserName = "flamengo";
        String expectedFullName = "regatas";
        user.updateProfile("flamengo", "regatas");

        assertNotNull(user, () -> "user cannot be null");
        assertEquals(expectedUserName, user.getUserName(), () -> "username not matches");
        assertEquals(expectedFullName, user.getFullName(), () -> "fullName not matches");
    }

    @Test
    @DisplayName("Test: change password")
    void test_ChangePassword() {
        user.changePassword("flamengo");

        assertNotNull(user, () -> "user cannot be null");
        assertEquals("flamengo", user.getPassword(), () -> "password not matches");
    }

    @Test
    @DisplayName("Test: remove a permission of user")
    void test_RevokePermissionOfUser() {
        String expectedUserName = "username";
        String expectedFullName = "fullName";
        String expectedPassword = "password";
        int expectedQuantityPermissions = 1;

        user.grantPermission(adminPermission);

        user.setFullName(" fullName ");

        assertNotNull(user, () -> "user cannot be null");
        assertEquals(expectedUserName, user.getUserName(), () -> "username not matches");
        assertEquals(expectedFullName, user.getFullName(), () -> "fullName not matches");
        assertEquals(expectedPassword, user.getPassword(), () -> "password not matches");
        assertEquals(expectedQuantityPermissions, user.getPermissions().size(), () -> "quantity permissions not matches");
        assertTrue(user.getPermissions().contains(adminPermission));
        assertFalse(user.getPermissions().contains(commonPermission));

        expectedQuantityPermissions = 0;

        user.revokePermission(adminPermission);
        assertEquals(expectedQuantityPermissions, user.getPermissions().size(), () -> "quantity permissions not matches");
    }
}