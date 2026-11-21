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

class DeleteUserUseCaseTest {

    private InMemoryUserRepository userRepository;
    private InMemoryPermissionRepository permissionRepository;
    private DeleteUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        permissionRepository = new InMemoryPermissionRepository();
        useCase = new DeleteUserUseCase(userRepository);
    }

    @Test
    @DisplayName("Test: when the user exists, should delete only that user")
    void test_WhenTheUserExists_ShouldDeleteOnlyThatUser() {
        User bob = UserFixtures.user("bob", permissionRepository.common);
        User alice = UserFixtures.user("alice", permissionRepository.common);

        userRepository.save(bob);
        userRepository.save(alice);

        useCase.execute(bob.getId());

        assertTrue(userRepository.findById(bob.getId()).isEmpty());
        assertTrue(userRepository.findById(alice.getId()).isPresent(), () -> "Other users must not be affected");
        assertEquals(1, userRepository.count());
    }

    @Test
    @DisplayName("Test: when the id is null, should throw exception")
    void test_WhenTheIdIsNull_ShouldThrowException() {
        assertThrows(RequiredObjectIsNullException.class, () -> useCase.execute(null));
    }

    @Test
    @DisplayName("Test: when the user does not exist, should throw exception and delete nothing")
    void test_WhenTheUserDoesNotExist_ShouldThrowExceptionAndDeleteNothing() {
        userRepository.save(UserFixtures.user("bob", permissionRepository.common));

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> useCase.execute(UUID.randomUUID()));

        assertTrue(exception.getMessage().contains("User not found!"));
        assertEquals(1, userRepository.count());
    }
}