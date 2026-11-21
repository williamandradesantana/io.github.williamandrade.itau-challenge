package io.github.williamandradesantana.itau_challenge.application.auth.usecases;

import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.ResourceNotFoundException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.User;
import io.github.williamandradesantana.itau_challenge.support.InMemoryPermissionRepository;
import io.github.williamandradesantana.itau_challenge.support.InMemoryUserRepository;
import io.github.williamandradesantana.itau_challenge.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DisableUserUseCaseTest {

    private InMemoryUserRepository userRepository;
    private InMemoryPermissionRepository permissionRepository;
    private DisableUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        permissionRepository = new InMemoryPermissionRepository();
        useCase = new DisableUserUseCase(userRepository);
    }

    @Test
    @DisplayName("Test: when the user exists, should disable only that user")
    void test_WhenTheUserExists_ShouldDisableOnlyThatUser() {
        User bob = UserFixtures.user("bob", permissionRepository.common);
        User alice = UserFixtures.user("alice", permissionRepository.common);
        userRepository.save(bob);
        userRepository.save(alice);

        useCase.execute(bob.getId());

        assertFalse(userRepository.findById(bob.getId()).orElseThrow().isEnabled());
        assertTrue(userRepository.findById(alice.getId()).orElseThrow().isEnabled(), () -> "other users must not be affected");
        assertEquals(2, userRepository.count(), () -> "disabling must not delete the user");
    }

    @Test
    @DisplayName("Test: when the user is already disabled, should not throw exception")
    void test_WhenTheUserIsAlreadyDisabled_ShouldNotThrowException() {
        User bob = UserFixtures.user("bob", permissionRepository.common);
        userRepository.save(bob);
        useCase.execute(bob.getId());

        assertDoesNotThrow(() -> useCase.execute(bob.getId()));
        assertFalse(userRepository.findById(bob.getId()).orElseThrow().isEnabled());
    }

    @Test
    @DisplayName("Test: when the id is null, should throw exception")
    void test_WhenTheIdIsNull_ShouldThrowException() {
        assertThrows(RequiredObjectIsNullException.class, () -> useCase.execute(null));
    }

    @Test
    @DisplayName("Test: when the user does not exist, should throw exception")
    void test_WhenTheUserDoesNotExist_ShouldThrowException() {
        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> useCase.execute(UUID.randomUUID()));

        assertTrue(exception.getMessage().contains("User not found"));
    }
}
