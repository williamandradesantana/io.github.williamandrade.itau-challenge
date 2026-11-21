package io.github.williamandradesantana.itau_challenge.application.auth.dtos;

import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.support.InMemoryPermissionRepository;
import io.github.williamandradesantana.itau_challenge.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserResultTest {

    private User user;
    private final InMemoryPermissionRepository permissionRepository = new InMemoryPermissionRepository();

    @BeforeEach
    void setUp() {
        user = UserFixtures.user("bob", permissionRepository.common, permissionRepository.admin);
    }

    @Test
    @DisplayName("Test: from should map ever field of the user")
    void test_FromShouldMapEveryFieldOfTheUser() {
        UUID expectedId = user.getId();
        String expectedUserName = "bob";
        String expectedFullName = "Full bob";
        boolean expectedEnabled = true;
        Set<String> expectedRoles = Set.of("COMMON", "ADMIN");

        UserResult result = UserResult.from(user);

        assertEquals(expectedId, result.id(), () -> "The id not matches");
        assertEquals(expectedUserName, result.userName(), () -> "The userName not matches");
        assertEquals(expectedFullName, result.fullName(), () -> "The fullName not matches");
        assertTrue(expectedEnabled, () -> "The user must be enabled");
        assertEquals(expectedRoles, result.roles(), () -> "The roles not matches");
    }

    @Test
    @DisplayName("Test: from should reflect a disabled user")
    void test_FromShouldReflectADisabledUser() {
        User user = UserFixtures.user("bob", permissionRepository.common);
        user.setEnabled(false);

        assertFalse(UserResult.from(user).enabled());
    }

    @Test
    @DisplayName("Test: the result must never expose the password")
    void test_TheResultMustNeverExposeThePassword() {
        User user = UserFixtures.user("bob", permissionRepository.common);

        UserResult result = UserResult.from(user);

        boolean hasPasswordField = Arrays.stream(UserResult.class.getRecordComponents())
                .map(RecordComponent::getName)
                .anyMatch(name -> name.toLowerCase().contains("password"));
        assertFalse(hasPasswordField, () -> "UserResult must not have any password component");
        assertFalse(result.toString().contains(user.getPassword()), () -> "toString must not leak the password hash");
    }

}